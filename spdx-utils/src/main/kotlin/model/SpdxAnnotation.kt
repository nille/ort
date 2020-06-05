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

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

import java.util.Date

/**
 * Annotation on a [SpdxDocument], [SpdxFile], or [SpdxPackage].
 */
data class SpdxAnnotation(
    /**
     * This field identifies the person, organization or tool that has commented on a file, package, or entire document.
     * Cardinality: Mandatory, one.
     */
    val annotator: String,

    /**
     * Identify when the comment was made.
     * This is to be specified according to the combined date and time in the UTC format,
     * as specified in the ISO 8601 standard.
     * Cardinality: Mandatory, one.
     */
    @get:JsonProperty("annotationDate")
    @get:JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )
    val date: Date,

    /**
     * Type of the annotation.
     * Cardinality: Mandatory, one.
     */
    @get:JsonProperty("annotationType")
    val type: SpdxAnnotationType,

    /**
     * Comments from Annotator.
     * Cardinality: Mandatory, one.
     */
    @get:JsonProperty("annotationComment")
    val comment: String

    ) : Comparable<SpdxAnnotation> {

    /**
     * A comparison function to sort [SpdxAnnotation]s.
     */
    override fun compareTo(other: SpdxAnnotation) =
        compareValuesBy(
            this,
            other,
            compareBy(SpdxAnnotation::date)
                .thenBy(SpdxAnnotation::type)
                .thenBy(SpdxAnnotation::annotator)
                .thenBy(SpdxAnnotation::comment)
        ) { it }
}
