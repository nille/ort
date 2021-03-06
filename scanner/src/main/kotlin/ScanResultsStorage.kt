/*
 * Copyright (C) 2017-2019 HERE Europe B.V.
 * Copyright (C) 2019 Bosch Software Innovations GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package org.ossreviewtoolkit.scanner

import org.ossreviewtoolkit.model.AccessStatistics
import org.ossreviewtoolkit.model.Failure
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.Package
import org.ossreviewtoolkit.model.Result
import org.ossreviewtoolkit.model.ScanResult
import org.ossreviewtoolkit.model.ScanResultContainer
import org.ossreviewtoolkit.model.ScannerDetails
import org.ossreviewtoolkit.model.Success
import org.ossreviewtoolkit.model.config.FileBasedStorageConfiguration
import org.ossreviewtoolkit.model.config.PostgresStorageConfiguration
import org.ossreviewtoolkit.model.config.ScannerConfiguration
import org.ossreviewtoolkit.scanner.storages.*
import org.ossreviewtoolkit.utils.ORT_FULL_NAME
import org.ossreviewtoolkit.utils.getOrtDataDirectory
import org.ossreviewtoolkit.utils.log
import org.ossreviewtoolkit.utils.storage.HttpFileStorage
import org.ossreviewtoolkit.utils.storage.LocalFileStorage
import org.ossreviewtoolkit.utils.storage.XZCompressedLocalFileStorage

import java.lang.IllegalArgumentException
import java.sql.DriverManager
import java.util.Properties

/**
 * The abstract class that storage backends for scan results need to implement.
 */
abstract class ScanResultsStorage {
    /**
     * A companion object that allow to configure the globally used storage backend.
     */
    companion object {
        /**
         * The scan result storage in use. Needs to be set via the corresponding configure function.
         */
        var storage: ScanResultsStorage = NoStorage()

        /**
         * Configure the [ScanResultsStorage]. If [config] does not contain a storage configuration by default a
         * [FileBasedStorage] using a [XZCompressedLocalFileStorage] as backend is configured.
         */
        fun configure(config: ScannerConfiguration) {
            val configuredStorages = listOfNotNull(
                config.fileBasedStorage,
                config.postgresStorage
            )

            require(configuredStorages.size <= 1) {
                "Only one scan results storage may be configured."
            }

            when (val storageConfig = configuredStorages.singleOrNull()) {
                null -> configureDefaultStorage()
                is FileBasedStorageConfiguration -> configure(storageConfig)
                is PostgresStorageConfiguration -> configure(storageConfig)
                else -> throw IllegalArgumentException(
                    "Unsupported configuration type '${storageConfig.javaClass.name}'."
                )
            }
        }

        private fun configureDefaultStorage() {
            val localFileStorage = XZCompressedLocalFileStorage(getOrtDataDirectory().resolve("$TOOL_NAME/results"))
            val fileBasedStorage = FileBasedStorage(localFileStorage)
            storage = fileBasedStorage
        }

        /**
         * Configure a [FileBasedStorage] as the current storage backend.
         */
        private fun configure(config: FileBasedStorageConfiguration) {
            val backend = config.backend.createFileStorage()

            when (backend) {
                is HttpFileStorage -> log.info { "Using file based storage with HTTP backend '${backend.url}'." }
                is LocalFileStorage -> log.info {
                    "Using file based storage with local directory '${backend.directory.invariantSeparatorsPath}'."
                }
            }

            storage = FileBasedStorage(backend)
        }

        /**
         * Configure a [PostgresStorage] as the current storage backend.
         */
        private fun configure(config: PostgresStorageConfiguration) {
            require(config.url.isNotBlank()) {
                "URL for PostgreSQL storage is missing."
            }

            require(config.schema.isNotBlank()) {
                "Database for PostgreSQL storage is missing."
            }

            require(config.username.isNotBlank()) {
                "Username for PostgreSQL storage is missing."
            }

            require(config.password.isNotBlank()) {
                "Password for PostgreSQL storage is missing."
            }

            val properties = Properties()
            properties["user"] = config.username
            properties["password"] = config.password
            properties["ApplicationName"] = "$ORT_FULL_NAME - $TOOL_NAME"

            // Configure SSL, see: https://jdbc.postgresql.org/documentation/head/connect.html
            // Note that the "ssl" property is only a fallback in case "sslmode" is not used. Since we always set
            // "sslmode", "ssl" is not required.
            properties["sslmode"] = config.sslmode
            config.sslcert?.let { properties["sslcert"] = it }
            config.sslkey?.let { properties["sslkey"] = it }
            config.sslrootcert?.let { properties["sslrootcert"] = it }

            val connection = DriverManager.getConnection(config.url, properties)

            storage = PostgresStorage(connection, config.schema).also { it.setupDatabase() }
        }
    }

    /**
     * The name to refer to this storage implementation.
     */
    open val name: String = javaClass.simpleName

    /**
     * The access statistics for the scan result storage.
     */
    val stats = AccessStatistics()

    /**
     * Read all [ScanResult]s for this [id] from the storage.
     *
     * @param id The [Identifier] of the scanned [Package].
     *
     * @return The [ScanResultContainer] for this [id].
     */
    fun read(id: Identifier): Result<ScanResultContainer> =
        readFromStorage(id).also {
            stats.numReads.incrementAndGet()
            if (it is Success && it.result.results.isNotEmpty()) {
                stats.numHits.incrementAndGet()
            }
        }

    /**
     * Read the [ScanResult]s matching the [id][Package.id] of [pkg] and the [scannerDetails] from the storage.
     * [ScannerDetails.isCompatible] is used to check if the results are compatible with the provided [scannerDetails].
     * Also [Package.sourceArtifact], [Package.vcs], and [Package.vcsProcessed] are used to check if the scan result
     * matches the expected source code location. This is important to find the correct results when different revisions
     * of a package using the same version name are used (e.g. multiple scans of 1.0-SNAPSHOT during development).
     *
     * @param pkg The [Package] to look up results for.
     * @param scannerDetails Details about the scanner that was used to scan the [Package].
     *
     * @return The [ScanResultContainer] matching the [id][Package.id] of [pkg] and the [scannerDetails].
     */
    fun read(pkg: Package, scannerDetails: ScannerDetails): Result<ScanResultContainer> =
        readFromStorage(pkg, scannerDetails).also {
            stats.numReads.incrementAndGet()
            if (it is Success && it.result.results.isNotEmpty()) {
                stats.numHits.incrementAndGet()
            }
        }

    /**
     * Add a [ScanResult] to the [ScanResultContainer] for this [id] and write it to the storage.
     *
     * @param id The [Identifier] of the scanned [Package].
     * @param scanResult The [ScanResult]. The [ScanResult.rawResult] must not be null.
     *
     * @return A [Result] describing if the operation was successful.
     */
    fun add(id: Identifier, scanResult: ScanResult): Result<Unit> {
        // Do not store empty scan results. It is likely that something went wrong when they were created, and if not,
        // it is cheap to re-create them.
        if (scanResult.summary.fileCount == 0) {
            val message = "Not storing scan result for '${id.toCoordinates()}' because no files were scanned."
            log.info { message }

            return Failure(message)
        }

        // Do not store scan results without raw result. The raw result can be set to null for other usages, but in the
        // storage it must never be null.
        if (scanResult.rawResult == null) {
            val message = "Not storing scan result for '${id.toCoordinates()}' because the raw result is null."
            log.info { message }

            return Failure(message)
        }

        // Do not store scan results without provenance information, because they cannot be assigned to the revision of
        // the package source code later.
        if (scanResult.provenance.sourceArtifact == null && scanResult.provenance.vcsInfo == null) {
            val message =
                "Not storing scan result for '${id.toCoordinates()}' because no provenance information is available."
            log.info { message }

            return Failure(message)
        }

        return addToStorage(id, scanResult)
    }

    protected abstract fun readFromStorage(id: Identifier): Result<ScanResultContainer>

    protected abstract fun readFromStorage(pkg: Package, scannerDetails: ScannerDetails): Result<ScanResultContainer>

    protected abstract fun addToStorage(id: Identifier, scanResult: ScanResult): Result<Unit>
}
