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

package org.ossreviewtoolkit

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.file

import org.ossreviewtoolkit.commands.*
import org.ossreviewtoolkit.model.Environment
import org.ossreviewtoolkit.model.config.OrtConfiguration
import org.ossreviewtoolkit.utils.ORT_NAME
import org.ossreviewtoolkit.utils.ORT_DATA_DIR_ENV_NAME
import org.ossreviewtoolkit.utils.expandTilde
import org.ossreviewtoolkit.utils.fixupUserHomeProperty
import org.ossreviewtoolkit.utils.getOrtDataDirectory
import org.ossreviewtoolkit.utils.printStackTrace

import java.io.File

import kotlin.system.exitProcess

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator

/**
 * Helper class for mutually exclusive command line options of different types.
 */
sealed class GroupTypes {
    data class FileType(val file: File) : GroupTypes()
    data class StringType(val string: String) : GroupTypes()
}

class OrtMain : CliktCommand(name = ORT_NAME, epilog = "* denotes required options.") {
    private val configFile by option("--config", "-c", help = "The path to a configuration file.")
        .convert { it.expandTilde() }
        .file(mustExist = true, canBeFile = true, canBeDir = false, mustBeWritable = false, mustBeReadable = true)

    private val logLevel by option(help = "Set the verbosity level of log output.").switch(
        "--info" to Level.INFO,
        "--debug" to Level.DEBUG
    ).default(Level.WARN)

    private val stacktrace by option(help = "Print out the stacktrace for all exceptions.").flag()

    private val configArguments by option(
        "-P",
        help = "Override a key-value pair in the configuration file. For example: " +
                "-P scanner.postgresStorage.schema=testSchema"
    ).associate()

    private val env = Environment()

    private inner class OrtHelpFormatter : CliktHelpFormatter(requiredOptionMarker = "*", showDefaultValues = true) {
        override fun formatHelp(
            prolog: String,
            epilog: String,
            parameters: List<HelpFormatter.ParameterHelp>,
            programName: String
        ) =
            buildString {
                // If help is invoked without a subcommand, the main run() is not invoked and no header is printed, so
                // we need to do that manually here.
                if (currentContext.invokedSubcommand == null) appendln(getVersionHeader(env.ortVersion))
                append(super.formatHelp(prolog, epilog, parameters, programName))
            }
    }

    init {
        fixupUserHomeProperty()

        context {
            expandArgumentFiles = false
            helpFormatter = OrtHelpFormatter()
        }

        subcommands(
            AnalyzerCommand(),
            ClearlyDefinedUploadCommand(),
            DownloaderCommand(),
            EvaluatorCommand(),
            ReporterCommand(),
            RequirementsCommand(),
            ScannerCommand()
        )

        versionOption(
            version = env.ortVersion,
            names = setOf("--version", "-v"),
            help = "Show version information and exit.",
            message = ::getVersionHeader
        )
    }

    override fun run() {
        Configurator.setRootLevel(logLevel)

        // Make the parameter globally available.
        printStackTrace = stacktrace

        // Make the OrtConfiguration available to subcommands.
        currentContext.findOrSetObject {
            OrtConfiguration.load(
                configArguments,
                configFile ?: getOrtDataDirectory().resolve("config/ort.conf")
            )
        }

        println(getVersionHeader(env.ortVersion))
    }

    private fun getVersionHeader(version: String): String {
        val variables = mutableListOf("$ORT_DATA_DIR_ENV_NAME = ${getOrtDataDirectory()}")
        env.variables.entries.mapTo(variables) { (key, value) -> "$key = $value" }

        val commandName = currentContext.invokedSubcommand?.commandName
        val command = commandName?.let { " '$commandName'" }.orEmpty()
        val with = if (variables.isNotEmpty()) " with" else "."

        var variableIndex = 0

        val header = mutableListOf<String>()
        """
            ________ _____________________
            \_____  \\______   \__    ___/ the OSS Review Toolkit, version $version.
             /   |   \|       _/ |    |    Running$command under Java ${env.javaVersion} on ${env.os}$with
            /    |    \    |   \ |    |    ${variables.getOrElse(variableIndex++) { "" }}
            \_______  /____|_  / |____|    ${variables.getOrElse(variableIndex++) { "" }}
                    \/       \/
        """.trimIndent().lines().mapTo(header) { it.trimEnd() }

        val moreVariables = variables.drop(variableIndex)
        if (moreVariables.isNotEmpty()) {
            header += "More environment variables:"
            header += moreVariables
        }

        return header.joinToString("\n", postfix = "\n")
    }
}

/**
 * The entry point for the application with [args] being the list of arguments.
 */
fun main(args: Array<String>) {
    OrtMain().main(args)
    exitProcess(0)
}
