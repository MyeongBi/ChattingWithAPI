package com.example.chattingwithapi
import android.os.AsyncTask
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class RequestTask : AsyncTask<String, Void, String>() {
    override fun doInBackground(vararg params: String?): String {
        var result = ""
        try {
            val url = URL(params[0])
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000 // 연결 제한 시간 설정

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                var line: String?
                val stringBuilder = StringBuilder()
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                reader.close()
                result = stringBuilder.toString()
            } else {
                result = "Error: $responseCode"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    override fun onPostExecute(result: String?) {
        // GET 요청 결과 처리
        // result 변수에 서버의 응답 데이터가 들어옵니다.
        // 필요한 로직을 구현하여 데이터를 처리해주세요.

        val c_activity = ChatRoomActivity()
        try {
            val jsonObject = JsonParser().parse(result).asJsonObject
            val divText1 = jsonObject?.get("divText1")?.asString
            val divText2 = jsonObject?.get("divText2")?.asString

            // onDataReceived() 메서드를 호출하여 데이터 전달
          //  c_activity.onDataReceived(divText1, divText2)
        } catch (e: Exception) {
            e.printStackTrace()
            // 오류 처리 로직 추가
        }
    }


}

