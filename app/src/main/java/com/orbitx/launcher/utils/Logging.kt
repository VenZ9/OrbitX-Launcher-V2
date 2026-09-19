package com.orbitx.launcher.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Logging utility for OrbitX Launcher
 * Provides both console and file logging
 */
object Logging {
    
    // Log levels
    const val VERBOSE = Log.VERBOSE
    const val DEBUG = Log.DEBUG
    const val INFO = Log.INFO
    const val WARN = Log.WARN
    const val ERROR = Log.ERROR
    
    // Tag for OrbitX logs
    private const val ORBITX_TAG = "OrbitX"
    
    // Date format for log files
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    
    // File date format
    private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    // Context for accessing files
    private var context: Context? = null
    
    // Logging enabled flags
    private var consoleLoggingEnabled = true
    private var fileLoggingEnabled = true
    private var logToFile = true
    
    // Log file
    private var logFile: File? = null
    
    /**
     * Initialize logging with context
     */
    fun initialize(context: Context) {
        this.context = context.applicationContext
        
        // Create log directory if it doesn't exist
        val logsDir = File(context.getExternalFilesDir(null), "logs")
        if (!logsDir.exists()) {
            logsDir.mkdirs()
        }
        
        // Create log file for today
        val logFileName = "orbitx_${fileDateFormat.format(Date())}.log"
        logFile = File(logsDir, logFileName)
        
        // Log initialization
        v(ORBITX_TAG, "Logging initialized")
    }
    
    /**
     * Set console logging enabled
     */
    fun setConsoleLoggingEnabled(enabled: Boolean) {
        consoleLoggingEnabled = enabled
    }
    
    /**
     * Set file logging enabled
     */
    fun setFileLoggingEnabled(enabled: Boolean) {
        fileLoggingEnabled = enabled
    }
    
    /**
     * Set log to file enabled
     */
    fun setLogToFile(enabled: Boolean) {
        logToFile = enabled
    }
    
    /**
     * Verbose log
     */
    fun v(tag: String, message: String) {
        log(VERBOSE, tag, message, null)
    }
    
    /**
     * Debug log
     */
    fun d(tag: String, message: String) {
        log(DEBUG, tag, message, null)
    }
    
    /**
     * Info log
     */
    fun i(tag: String, message: String) {
        log(INFO, tag, message, null)
    }
    
    /**
     * Warning log
     */
    fun w(tag: String, message: String) {
        log(WARN, tag, message, null)
    }
    
    /**
     * Error log
     */
    fun e(tag: String, message: String) {
        log(ERROR, tag, message, null)
    }
    
    /**
     * Verbose log with throwable
     */
    fun v(tag: String, message: String, throwable: Throwable) {
        log(VERBOSE, tag, message, throwable)
    }
    
    /**
     * Debug log with throwable
     */
    fun d(tag: String, message: String, throwable: Throwable) {
        log(DEBUG, tag, message, throwable)
    }
    
    /**
     * Info log with throwable
     */
    fun i(tag: String, message: String, throwable: Throwable) {
        log(INFO, tag, message, throwable)
    }
    
    /**
     * Warning log with throwable
     */
    fun w(tag: String, message: String, throwable: Throwable) {
        log(WARN, tag, message, throwable)
    }
    
    /**
     * Error log with throwable
     */
    fun e(tag: String, message: String, throwable: Throwable) {
        log(ERROR, tag, message, throwable)
    }
    
    /**
     * Generic log method
     */
    private fun log(level: Int, tag: String, message: String, throwable: Throwable?) {
        val fullTag = "$ORBITX_TAG-$tag"
        val timestamp = dateFormat.format(Date())
        val logMessage = "[$timestamp] [$fullTag] $message"
        
        // Console logging
        if (consoleLoggingEnabled) {
            when (level) {
                VERBOSE -> Log.v(fullTag, message, throwable)
                DEBUG -> Log.d(fullTag, message, throwable)
                INFO -> Log.i(fullTag, message, throwable)
                WARN -> Log.w(fullTag, message, throwable)
                ERROR -> Log.e(fullTag, message, throwable)
            }
        }
        
        // File logging
        if (fileLoggingEnabled && logToFile && logFile != null) {
            writeToFile(logMessage, throwable)
        }
    }
    
    /**
     * Write log message to file
     */
    private fun writeToFile(message: String, throwable: Throwable?) {
        try {
            logFile?.let { file ->
                FileWriter(file, true).use { writer ->
                    writer.write("$message\n")
                    throwable?.let { t ->
                        writer.write("Stack Trace:\n")
                        t.stackTrace.forEach { element ->
                            writer.write("  at ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})\n")
                        }
                    }
                }
            }
        } catch (e: IOException) {
            // Can't log to file, but we don't want to crash
            e.printStackTrace()
        }
    }
    
    /**
     * Log exception
     */
    fun logException(tag: String, throwable: Throwable, message: String? = null) {
        val errorMessage = message ?: throwable.message ?: "Unknown error"
        e(tag, errorMessage, throwable)
    }
    
    /**
     * Log method entry
     */
    fun logMethodEntry(tag: String, method: String, vararg args: Any?) {
        val argsString = args.joinToString(", ") { it?.toString() ?: "null" }
        v(tag, "Entering $method($argsString)")
    }
    
    /**
     * Log method exit
     */
    fun logMethodExit(tag: String, method: String, result: Any? = null) {
        val resultString = result?.toString() ?: "void"
        v(tag, "Exiting $method -> $resultString")
    }
    
    /**
     * Log timing
     */
    fun logTiming(tag: String, operation: String, startTime: Long) {
        val duration = System.currentTimeMillis() - startTime
        d(tag, "$operation completed in ${duration}ms")
    }
    
    /**
     * Get log file content
     */
    fun getLogFileContent(): String? {
        return try {
            logFile?.readText()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get all log files
     */
    fun getLogFiles(): List<File> {
        val logsDir = context?.let { File(it.getExternalFilesDir(null), "logs") }
        return logsDir?.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    /**
     * Delete log file
     */
    fun deleteLogFile(file: File): Boolean {
        return try {
            file.delete()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Delete all log files
     */
    fun deleteAllLogFiles() {
        getLogFiles().forEach { file ->
            file.delete()
        }
    }
    
    /**
     * Share log file
     */
    fun shareLogFile(context: Context, file: File) {
        // Implementation would use Android's share intent
        // This is a placeholder for the actual implementation
    }
}
