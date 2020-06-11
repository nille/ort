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

private const val SPDX_VERSION = "SPDX-2.2"
private const val DATA_LICENSE = "CC0-1.0"

/**
 * An SPDX document as specified by https://github.com/spdx/spdx-spec/tree/master/chapters and
 * https://github.com/spdx/spdx-spec/blob/development/v2.2.1/examples/.
 */
data class SpdxDocument(
    /**
     * Identifier og this [SpdxDocument] which may be referenced in relationships by other files, packages internally
     * and documents externally.
     *
     * TODO: Introduce a dedicated type.
     */
    @JsonProperty("SPDXID")
    val spdxId: String,

    /**
     * The SPDX version of this document, must equal [SPDX_VERSION].
     */
    val spdxVersion: String = SPDX_VERSION,

    /**
     * Information about the creation of this document.
     */
    val creationInfo: SpdxCreationInfo,

    /**
     * The name of this [SpdxDocument] as a single line.
     */
    val name: String,

    /**
     * The data license of this document, must equal [DATA_LICENSE].
     */
    val dataLicense: String = DATA_LICENSE,

    /**
     * A comment towards the consumers of this [SpdxDocument] as multi-line text.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val comment: String = "",

    /**
     * A listing of any external [SpdxDocument] referenced from within this [SpdxDocument].
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val externalDocumentRefs: List<SpdxExternalDocumentReference> = emptyList(),

    /**
     * Information about any licenses which are not on the SPDX license list.
     */
    val hasExtractedLicensingInfos: List<SpdxExtractedLicenseInfo> = emptyList(),

    /**
     * The [SpdxAnnotation]s for the [SpdxDocument]..
     */
    val annotations: List<SpdxAnnotation> = emptyList(),

    /**
     * A unique absolute Uniform Resource Identifier (URI) as specified in RFC-3986, with the following
     * exceptions:
     *
     *  - The SPDX Document URI cannot contain a URI "part" (e.g. the # delimiter), since the # is used to uniquely
     *    identify SPDX element identifiers. The URI must contain a scheme (e.g. https:).
     *  - The URI must be unique for the SPDX document including the specific version of the SPDX document. If the SPDX
     *    document is updated, thereby creating a new version, a new URI for the updated document must be used. There
     *    can only be one URI for an SPDX document and only one SPDX document for a given URI.
     */
    val documentNamespace: String,

    /**
     * All SPDX identifiers of all packages and files contained in [packages] and [files].
     */
    val documentDescribes: List<String> = emptyList(),

    /**
     * All packages described in this [SpdxDocument].
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val packages: List<SpdxPackage> = emptyList(),

    /**
     * All files described in this [SpdxDocument].
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val files: List<SpdxFile> = emptyList(),

    /**
     * All snippets described in this [SpdxDocument].
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val snippets: List<SpdxSnippet> = emptyList(),

    /**
     * All relationships described in this [SpdxDocument].
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val relationships: List<SpdxRelationship> = emptyList()
) {
    init {
        require(spdxId.isNotBlank()) { "The 'SPDXID' must not be blank. "}

        require(spdxVersion == SPDX_VERSION) { "The 'SPDXVersion' must be $SPDX_VERSION, but was $spdxVersion." }

        require(name.isNotBlank()) { "The 'name' must not be blank. "}

        require(dataLicense == DATA_LICENSE) { "The 'dataLicense' must be $DATA_LICENSE, but was $dataLicense." }

        require(documentNamespace.isNotBlank()) { "The 'documentNamespace' must not be blank. "}
    }
}
