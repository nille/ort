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

package org.ossreviewtoolkit.spdx

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object SpdxModelSerializer {
    fun createYamlMapper(): ObjectMapper = YAMLMapper().apply(mapperConfig)

    fun createJsonMapper(): ObjectMapper = JsonMapper().apply(mapperConfig)

    fun toYaml(obj: Any): String = createYamlMapper().writeValueAsString(obj)

    inline fun <reified T : Any> fromYaml(yaml: String): T = createYamlMapper().readValue(yaml)

    fun toJson(obj: Any): String = createJsonMapper().writeValueAsString(obj)

    inline fun <reified T : Any> fromJson(yaml: String): T = createJsonMapper().readValue(yaml)

}

private val mapperConfig: ObjectMapper.() -> Unit = {
    registerKotlinModule()

    registerModule(JavaTimeModule())
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    enable(SerializationFeature.INDENT_OUTPUT)
}
