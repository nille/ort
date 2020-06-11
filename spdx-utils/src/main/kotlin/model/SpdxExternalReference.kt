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
import com.fasterxml.jackson.annotation.JsonValue

/**
 *  References an external source of additional information, metadata, enumerations, asset identifiers, or downloadable
 *  content believed to be relevant to the Package, see also
 *  https://github.com/spdx/spdx-spec/blob/master/chapters/3-package-information.md#321-external-reference-.
 */
class SpdxExternalReference(
    /**
     * Human-readable information about the purpose and target of the reference.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val comment: String = "",

    /**
     * The category of this reference which corresponds to the [referenceType].
     */
    val referenceCategory: Category,

    /**
     *  The unique string with no spaces necessary to access the package-specific information, metadata, or content
     *  within the target location. The format of the locator is subject to constraints defined by the [referenceType].
     */
    val referenceLocator: String,

    /**
     * TODO: Introduce an enum for the types.
     * https://github.com/spdx/spdx-spec/blob/master/chapters/appendix-VI-external-repository-identifiers.md
     */
    val referenceType: String
) {
    enum class Category(
        @JsonValue
        private val serializedName: String
    ) {
        SECURITY("SECURITY"),
        PACKAGE_MANAGER("PACKAGE-MANAGER"),
        PERSISTENT_ID("PERSISTENT-ID"),
        OTHER("OTHER");
    }

    init {
        require(referenceLocator.isNotBlank()) { "The referenceLocator must not be blank." }

        require(referenceType.isNotBlank()) { "The referenceType must not be blank." }
    }
}
