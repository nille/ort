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

package org.ossreviewtoolkit.spdx.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Describes a software package
 */
data class SpdxPackage(
    /**
     * Name as given by package originator.
     * Cardinality: Mandatory, one.
     */
    val name: String,

    /**
     * Identifier for the package.
     * Cardinality: Mandatory, one.
     */
    @JsonProperty("SPDXID")
    val id: String,

    /**
     * Version of the package.
     * Cardinality: Optional, one.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("packageVersion")
    val version: String? = null,

    /**
     * File name of the package, or path of the directory being treated as a package.
     * Cardinality: Optional, one.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("packageFileName")
    val fileName: String? = null,

    /**
     * Distribution source for the package/directory identified.
     * Cardinality: Optional, one.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("packageSupplier")
    val supplier: String? = null,

    /**
     * Identifies from where or whom the package originally came.
     * Cardinality: Optional, one.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("packageOriginator")
    val originator: String? = null,

    /**
     * List of [SpdxPackage] packages contained in document.
     */
    var files: List<SpdxFile> = emptyList(),

    /**
     * Indicates whether the file content of this package
     * has been available for or subjected to analysis
     * when the SPDX document was created.
     * Cardinality: Optional, one.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val filesAnalyzed: Boolean = true // TODO: this one is optional. What is the default value by spec?

    ) : Comparable<SpdxPackage> {
    companion object {
        /**
         * A constant for a [SpdxPackage] where all properties are empty.
         */
        @JvmField
        val EMPTY = SpdxPackage(
            name = "",
            id = "",
            version = "",
            fileName = null,
            supplier = null,
            originator = null,
            files = emptyList<SpdxFile>(),
            filesAnalyzed = true
        )
    }

    /**
     * A comparison function to sort packages by their SPDX id.
     */
    override fun compareTo(other: SpdxPackage) = id.compareTo(other.id)
}
