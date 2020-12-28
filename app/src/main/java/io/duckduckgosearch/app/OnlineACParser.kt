package io.duckduckgosearch.app

import android.os.AsyncTask
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import java.io.*
import java.net.*
import java.util.*

class OnlineACParser(private val filterResults: Array<String>?) : AsyncTask<String?, Void?, ArrayList<String>?>() {
    private var onParsed: OnParsed? = null

    interface OnParsed {
        fun onParsed(list: ArrayList<String>?)
    }

    fun setOnParseListener(onParsed: OnParsed?) {
        this.onParsed = onParsed
    }

    override fun doInBackground(vararg p0: String?): ArrayList<String>? {
        return getJSONAC(p0[0])
    }

    override fun onPostExecute(strings: ArrayList<String>?) {
        super.onPostExecute(strings)
        onParsed!!.onParsed(strings)
    }

    private fun getJSONAC(term: String?): ArrayList<String>? {
        val finalResponse = ArrayList<String>()
        if (term == null || term == "") {
            return null
        }
        val jsonString: String?
        jsonString = try {
            getJSON(ACUrl + URLEncoder.encode(term.trim(), "UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            return null
        }
        val suggestions: JSONArray
        try {
            suggestions = JSONArray(jsonString)
            for (i in 0 until suggestions.length()) {
                var isInHistory = false
                if (filterResults != null) {
                    for (filterResult in filterResults) {
                        if (filterResult == suggestions.getJSONObject(i).getString("phrase")) {
                            isInHistory = true
                        }
                    }
                }
                if (!isInHistory) {
                    finalResponse.add(suggestions.getJSONObject(i).getString("phrase"))
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return finalResponse
    }

    private fun getJSON(baseUrl: String): String? {
        var response: String? = null
        try {
            val url = URL(baseUrl)
            val urlConnection: HttpURLConnection
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            val inputStream: InputStream = BufferedInputStream(urlConnection.inputStream)
            response = streamToStr(inputStream)
        } catch (e: MalformedURLException) {
            Log.e("Error", "MalformedURLException: " + e.message)
        } catch (e: ProtocolException) {
            Log.e("Error", "ProtocolException: " + e.message)
        } catch (e: IOException) {
            Log.e("Error", "IOException: " + e.message)
        } catch (e: Exception) {
            Log.e("Error", "Exception: " + e.message)
            e.printStackTrace()
        }
        return response
    }

    private fun streamToStr(stream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(stream))
        val builder = StringBuilder()
        var line: String?
        try {
            while (reader.readLine().also { line = it } != null) {
                builder.append(line).append('\n')
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return builder.toString()
    }

    companion object {
        private const val ACUrl = "https://duckduckgo.com/ac/?q="
    }
}