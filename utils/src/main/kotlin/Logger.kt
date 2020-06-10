/*
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

package org.ossreviewtoolkit.utils

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.kotlin.KotlinLogger
import org.apache.logging.log4j.kotlin.loggerOf

import java.util.concurrent.ConcurrentHashMap

/**
 * Global map of loggers for classes so only one logger needs to be instantiated per class.
 */
val loggerOfClass = ConcurrentHashMap<Any, KotlinLogger>()

/**
 * An extension property for adding a log instance to any (unique) class.
 */
val <reified T : Any> T.log: KotlinLogger
    inline get() = loggerOfClass.getOrPut(T::class.java) { loggerOf(T::class.java) }

private val KotlinLogger.statements: MutableSet<Pair<Level, String>>
    get() = mutableSetOf()

fun KotlinLogger.infoOnce(supplier: () -> String) {
    if (log.delegate.isInfoEnabled) {
        val statement = Level.INFO to supplier()
        if (statement !in statements) {
            statements += statement
            log.info(statement.second)
        }
    }
}

fun KotlinLogger.warnOnce(supplier: () -> String) {
    if (log.delegate.isWarnEnabled) {
        val statement = Level.WARN to supplier()
        if (statement !in statements) {
            statements += statement
            log.warn(statement.second)
        }
    }
}
