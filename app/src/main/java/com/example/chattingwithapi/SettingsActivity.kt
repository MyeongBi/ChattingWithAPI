package com.example.chattingwithapi

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this@SettingsActivity, MainActivity::class.java))
                finish()  // 현재 액티비티 종료
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

        private val NOTIFICATION_SETTINGS_REQUEST_CODE = 100

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            // SwitchPreferenceCompat 객체 가져오기
            val attachment2Pref = findPreference<SwitchPreferenceCompat>("attachment2")
            val attachment4Pref = findPreference<SwitchPreferenceCompat>("attachment4")

            // 변경 리스너 등록
            attachment2Pref?.onPreferenceChangeListener = this
            attachment4Pref?.onPreferenceChangeListener = this
        }

        override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
            when (preference.key) {
                "attachment2" -> {
                    val isChecked = newValue as? Boolean ?: false
                    if (isChecked) {
                        enableAppNotification()
                    } else {
                        disableAppNotification()
                    }
                }
                "attachment4" -> {
                    val isChecked = newValue as? Boolean ?: false
                    if (isChecked) {
                        enableAppNotification()
                    } else {
                        disableAppNotification()
                    }
                }
            }
            return true
        }

        override fun onResume() {
            super.onResume()
            // 알림 설정 상태를 확인하여 스위치 상태 업데이트
            updateSwitchState()
        }

        private fun updateSwitchState() {
            val attachment2Pref = findPreference<SwitchPreferenceCompat>("attachment2")
            val isNotificationEnabled = isAppNotificationEnabled()
            attachment2Pref?.isChecked = isNotificationEnabled
        }

        private fun isAppNotificationEnabled(): Boolean {
            val notificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.areNotificationsEnabled()
            } else {
                NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
            }
        }

        private fun enableAppNotification() {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            startActivityForResult(intent, NOTIFICATION_SETTINGS_REQUEST_CODE)
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == NOTIFICATION_SETTINGS_REQUEST_CODE) {
                // 알림 권한 설정 창에서 돌아왔을 때 스위치 상태 업데이트
                updateSwitchState()
            }
        }

        private fun disableAppNotification() {
            // 앱 알림 설정을 끄는 코드 작성
            val notificationManager =
                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Android Oreo (API 레벨 26) 이상에서는 알림 채널을 삭제해야 함
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = "my_channel_id"
                notificationManager.deleteNotificationChannel(channelId)
            }

            // 알림 설정을 끌 때 수행할 동작
            // 예: 알림 권한을 취소하는 다이얼로그 표시
            if (NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                startActivity(intent)
            }
        }
    }
}
