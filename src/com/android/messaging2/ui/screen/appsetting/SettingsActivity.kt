package com.android.messaging2.ui.screen.appsetting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.loader.app.LoaderManager
import com.android.messaging.datamodel.DataModel
import com.android.messaging.datamodel.binding.BindingBase
import com.android.messaging.datamodel.data.SettingsData
import com.android.messaging2.ui.theme.MessagingTheme

class SettingsActivity : ComponentActivity(), SettingsData.SettingsDataListener {

    private val mBinding = BindingBase.createBinding<SettingsData>(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.bind(DataModel.get().createSettingsData(applicationContext, this))
        mBinding.data.init(LoaderManager.getInstance(this), mBinding)
        setContent {
            MessagingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingScreen(
                        modifier = Modifier,
                        settingsItemList = mBinding.data.settingsItems,
                        onBack = {
                            onBackPressed()
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mBinding.unbind()
        mBinding.bind(DataModel.get().createSettingsData(applicationContext, this))
        mBinding.data.init(LoaderManager.getInstance(this), mBinding)
    }

    override fun onSelfParticipantDataLoaded(data: SettingsData?) {
        mBinding.ensureBound(data)
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.unbind()
    }
}

