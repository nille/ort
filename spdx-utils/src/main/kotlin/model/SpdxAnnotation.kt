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

import java.time.Instant

/**
 * An annotation which can relate to [SpdxDocument]s, [SpdxFile]s, or [SpdxPackage]s, see also
 * https://github.com/spdx/spdx-spec/blob/master/chapters/8-annotations.md.
 */
data class SpdxAnnotation(
    /**
     * The creation date of this [Annotation].
     */
    val annotationDate: Instant,

    /**
     * The type of this [Annotation].
     */
    val annotationType: Type,

    /**
     * The person, organization or tool that has created this [Annotation]. The value must be a single line text in one
     * of the following formats:
     *
     * 1. "Person: person name" or "Person: person name (email)"
     * 2. "Organization: organization name" or "Organization: organization name (email)"
     * 3. "Tool: tool identifier - version"
     *
     * TODO: Introduce a data type for above subjects.
     */
    val annotator: String,

    /**
     * Comments from the [annotator].
     */
    val comment: String
) {
    enum class Type {
        /**
         * Type of annotation which does not fit in any of the pre-defined annotation types.
         */
        OTHER,

        /**
         * A Review represents an audit and signoff by an individual, organization
         * or tool on the information for an SpdxElement.
         */
        REVIEW;
    }

    init {
        require(annotator.isNotBlank()) { "The annotator must not be blank." }
    }
}

