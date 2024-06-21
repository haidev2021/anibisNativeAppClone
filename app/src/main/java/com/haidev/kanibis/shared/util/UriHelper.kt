
package com.haidev.kanibis.shared.util

import android.net.Uri
import android.text.TextUtils
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.NameValuePair
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.utils.URLEncodedUtils
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.message.BasicNameValuePair
import java.net.URI
import java.net.URISyntaxException

object UriHelper {
    /**
     * Encodes the key and value and then appends the parameter to the query
     * string.
     *
     * @param builder
     * the URI builder.
     * @param key
     * the key of the parameter.
     * @param value
     * the value of the parameter.
     */
    fun appendIntQueryParameter(
        builder: Uri.Builder,
        key: String?, value: Int
    ): Uri.Builder {
        return builder.appendQueryParameter(key, value.toString())
    }

    /**
     * Encodes the key and value and then appends the parameter with the value
     * "false" or "true" to the query string.
     *
     * @param builder
     * the URI builder.
     * @param key
     * the key of the parameter.
     * @param value
     * the value of the parameter.
     */
    fun appendBooleanQueryParameter(
        builder: Uri.Builder,
        key: String?, value: Boolean
    ): Uri.Builder {
        return builder.appendQueryParameter(key, value.toString())
    }

    /**
     * If the value of the parameter is not empty (null or 0-length), it encodes
     * the key and value and then appends the parameter to the query string.
     *
     * @param builder
     * the URI builder.
     * @param key
     * the key of the parameter.
     * @param value
     * the value of the parameter.
     */
    fun appendQueryParameterIfNotEmpty(
        builder: Uri.Builder, key: String?, value: String?
    ): Uri.Builder {
        return if (!TextUtils.isEmpty(value)) builder.appendQueryParameter(key, value) else builder
    }

    /**
     * If the value of the parameter is true, it encodes the key and value and
     * then appends the parameter with the value "true" to the query string.
     *
     * @param builder
     * the URI builder.
     * @param key
     * the key of the parameter.
     * @param value
     * the value of the parameter.
     */
    fun appendQueryParameterIfNotEmpty(
        builder: Uri.Builder, key: String?, value: Boolean?
    ): Uri.Builder {
        return if (value != null && value) builder.appendQueryParameter(key, "true") else builder
    }

    /**
     * If the value of the parameter is greater 0 (or not null), it encodes the key
     * and value and then appends the parameter to the query string.
     *
     * @param builder
     * the URI builder.
     * @param key
     * the key of the parameter.
     * @param value
     * the value of the parameter.
     */
    fun appendQueryParameterIfNotEmpty(
        builder: Uri.Builder, key: String?, value: Int?
    ): Uri.Builder {
        return if (value != null && value > 0) appendIntQueryParameter(
            builder,
            key,
            value
        ) else builder
    }

    /**
     * Gets the encoded query string.
     *
     * @param builder
     * the URI builder.
     * @return the encoded query string or null if there is't one.
     */
    fun getQueryString(builder: Uri.Builder?): String? {
        if (builder != null) {
            var queryString = builder.build().encodedQuery
            if (!TextUtils.isEmpty(queryString)) // replace URL encoded commas with decoded commas
                queryString = queryString!!.replace("%2C", ",")
            return queryString
        }
        return null
    }

    /**
     * Encodes the key and values and then appends the parameter with all values
     * separated by the separator string to the query string.
     *
     * @param builder
     * the URI builder.
     * @param key
     * the key of the parameter.
     * @param values
     * the values of the parameter.
     * @param valueSeparator
     * the character(s) separating the values.
     */
    fun appendListQueryParameter(
        builder: Uri.Builder,
        key: String?, values: Iterable<*>?, valueSeparator: String?
    ): Uri.Builder {
        return if (values != null) builder.appendQueryParameter(
            key, TextUtils.join(
                valueSeparator ?: ",", values
            )
        ) else builder
    }

    /**
     * Gets a list of query string parameters.
     *
     * @param queryString
     * the query string.
     * @return the list of parameters or null if the URI syntax is wrong.
     */
    fun getQueryStringParameters(
        queryString: String?
    ): MutableList<NameValuePair>? {
        var queryString = queryString ?: return null
        return try {
            if (!queryString.startsWith("?")) queryString = "?$queryString"
            URLEncodedUtils
                .parse(URI(queryString), "UTF-8")
        } catch (e: URISyntaxException) {
            null
        }
    }

    /**
     * Gets a list of query string parameter names.
     *
     * @param queryString
     * the query string.
     * @return the list of parameter names.
     */
    fun getQueryStringParameterNames(queryString: String?): List<String> {
        val parameterNames: MutableList<String> = ArrayList()
        val nameValues: List<NameValuePair>? = getQueryStringParameters(queryString)
        if (nameValues != null) {
            for (nvp in nameValues) parameterNames.add(nvp.getName())
        }
        return parameterNames
    }

    /**
     * Gets the parameter and values of the query string.
     *
     * @param queryString
     * the query string.
     * @return the map of parameter (key) and values (value).
     */
    fun getQueryStringParameterValues(
        queryString: String?
    ): HashMap<String, MutableList<String>> {
        val map = HashMap<String, MutableList<String>>()
        for (nvp in getQueryStringParameters(queryString)!!) {
            var values = map[nvp.getName()]
            if (values == null) {
                values = ArrayList()
                map[nvp.getName()] = values
            }
            val valueString: String = nvp.getValue()
            if (!TextUtils.isEmpty(valueString)) {
                val paramValues = valueString.split(",".toRegex()).toTypedArray()
                for (v in paramValues) values.add(v)
            }
        }
        return map
    }

    /**
     * replaces a parameter value.
     *
     * @param queryString
     * the query string.
     * @param parameterName
     * the parameter name for which the value will be replaced.
     * @param newValue
     * the new value for the parameter name.
     * @return the new query string with the replaced value.
     */
    fun replaceParameterValue(
        queryString: String?,
        parameterName: String?, newValue: String?
    ): String {
        val pairs: List<NameValuePair>? = getQueryStringParameters(queryString)
        val newPairs: MutableList<NameValuePair> = ArrayList<NameValuePair>()
        if (pairs != null) {
            for (pair in pairs) {
                if (pair.getName().equals(parameterName)) {
                    newPairs.add(BasicNameValuePair(pair.getName(), newValue))
                } else newPairs.add(pair)
            }
        }
        var url: String = URLEncodedUtils.format(newPairs, "utf-8")
        if (!TextUtils.isEmpty(url)) // replace URL encoded commas with decoded commas
            url = url.replace("%2C", ",")
        return url
    }
}
