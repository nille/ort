/*
 * Copyright (C) 2017-2019 HERE Europe B.V.
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

package org.ossreviewtoolkit.reporter

import org.ossreviewtoolkit.model.AnalyzerResult
import org.ossreviewtoolkit.model.OrtResult
import org.ossreviewtoolkit.model.ScanRecord
import org.ossreviewtoolkit.model.config.PathExclude
import org.ossreviewtoolkit.model.config.ScopeExclude
import org.ossreviewtoolkit.utils.joinNonBlank

import java.io.OutputStream
import java.util.ServiceLoader

/**
 * A reporter that creates a human readable report from the [AnalyzerResult] and [ScanRecord] contained in an
 * [OrtResult]. The signatures of public functions in this class define the library API.
 */
interface Reporter {
    companion object {
        private val LOADER = ServiceLoader.load(Reporter::class.java)!!

        /**
         * The list of all available reporters in the classpath.
         */
        val ALL by lazy { LOADER.iterator().asSequence().toList() }
    }

    /**
     * The name to use to refer to the reporter.
     */
    val reporterName: String

    /**
     * The default output filename to use with this reporter format.
     */
    val defaultFilename: String

    /**
     * Generate a report for the provided [input] and write the result to the [outputStream]. If and how the [input]
     * data is used depends on the specific reporter implementation, taking into account any format-specific [options].
     */
    fun generateReport(outputStream: OutputStream, input: ReporterInput, options: Map<String, String> = emptyMap())
}

internal val PathExclude.description: String get() = joinNonBlank(reason.toString(), comment)

internal val ScopeExclude.description: String get() = joinNonBlank(reason.toString(), comment)
