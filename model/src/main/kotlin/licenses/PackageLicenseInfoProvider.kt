/*
 * Copyright (C) 2017-2020 HERE Europe B.V.
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

package org.ossreviewtoolkit.model.licenses

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.OrtResult
import org.ossreviewtoolkit.model.Provenance
import org.ossreviewtoolkit.utils.ProcessedDeclaredLicense

/**
 * A provider for [PackageLicenseInfo]s created from an [ortResult].
 */
class PackageLicenseInfoProvider(val ortResult: OrtResult) {
    private val licenseInfo: MutableMap<Identifier, PackageLicenseInfo> = mutableMapOf()

    /**
     * Get the [PackageLicenseInfo] for the project or package identified by [id].
     */
    fun get(id: Identifier) = licenseInfo.getOrPut(id) { createLicenseInfo(id) }

    private fun createLicenseInfo(id: Identifier): PackageLicenseInfo {
        require(id in ortResult.getProjectAndPackageIds()) {
            "The ORT result does not contain a project or package with id '${id.toCoordinates()}'."
        }

        return PackageLicenseInfo(
            id = id,
            concludedLicenseInfo = createConcludedLicenseInfo(id),
            declaredLicenseInfo = createDeclaredLicenseInfo(id),
            detectedLicenseInfo = createDetectedLicenseInfo(id)
        )
    }

    private fun createConcludedLicenseInfo(id: Identifier): ConcludedLicenseInfo =
        ortResult.getPackage(id)?.let { curatedPkg ->
            ConcludedLicenseInfo(
                concludedLicense = curatedPkg.pkg.concludedLicense,
                appliedCurations = curatedPkg.curations.filter { it.curation.concludedLicense != null }
            )
        } ?: ConcludedLicenseInfo(concludedLicense = null, appliedCurations = emptyList())

    private fun createDeclaredLicenseInfo(id: Identifier): DeclaredLicenseInfo =
        ortResult.getProject(id)?.let { project ->
            DeclaredLicenseInfo(
                licenses = project.declaredLicenses,
                processed = project.declaredLicensesProcessed,
                appliedCurations = emptyList()
            )
        } ?: ortResult.getPackage(id)?.let { curatedPkg ->
            DeclaredLicenseInfo(
                licenses = curatedPkg.pkg.declaredLicenses,
                processed = curatedPkg.pkg.declaredLicensesProcessed,
                appliedCurations = curatedPkg.curations.filter { it.curation.declaredLicenses != null }
            )
        } ?: DeclaredLicenseInfo(
            licenses = emptySet(),
            processed = ProcessedDeclaredLicense(
                spdxExpression = null,
                unmapped = emptyList()
            ),
            appliedCurations = emptyList()
        )

    private fun createDetectedLicenseInfo(id: Identifier): DetectedLicenseInfo {
        val findings = mutableMapOf<Provenance, Findings>()

        ortResult.getScanResultsForId(id).forEach { scanResult ->
            findings[scanResult.provenance] = Findings(
                licenses = scanResult.summary.licenseFindings,
                copyrights = scanResult.summary.copyrightFindings
            )
        }

        return DetectedLicenseInfo(findings)
    }
}
