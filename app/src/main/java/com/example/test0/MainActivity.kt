package com.example.test0

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.fastjson.JSON
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.test0.activity.StreetSceneryActivity
import com.example.test0.activity.WebActivity
import com.example.test0.activity.WebInfoActivity
import com.example.test0.adapter.StreetMainAdapter
import com.example.test0.adapter.WebInfoAdapter
import com.example.test0.base.NetConstants
import com.example.test0.bean.VoiceNavBean
import com.example.test0.bean.VoiceReplyBean
import com.example.test0.bean.WeatherNowBean
import com.example.test0.utlis.JsonParser
import com.example.test0.utlis.ToastUtils
import com.example.test0.utlis.VoiceDialog
import com.google.gson.JsonObject
import com.iflytek.cloud.*
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject


class MainActivity : AppCompatActivity(), View.OnClickListener {

    var streetMainAdapter: StreetMainAdapter? = null
    var strListGoverment: MutableList<String> =
        mutableListOf("街道简介", "问卷调查", "留言建议", "投票管理", "在线办事") //政务服务数据
    var strListParty: MutableList<String> = mutableListOf("党务中心", "活动中心", "学习中心") //党建平台数据
    var strListPublicity: MutableList<String> = mutableListOf("党务中心", "活动中心", "学习中心") //宣传平台数据
    var strListForPeople: MutableList<String> = mutableListOf("交通状况", "通知公告", "疫情信息") //便民服务数据
    var strListGis: MutableList<String> = mutableListOf("街道实景", "建筑物信息") //便民服务数据
    var requestQueue: RequestQueue? = null
    var voiceDialog: VoiceDialog? = null
    var weatherNowBean: WeatherNowBean? = null
    //讯飞语音识别相关参数
    var mIat: SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        GetPermission()
        mIat = SpeechRecognizer.createRecognizer(this, mInitListener)

        initView()
        initHttpListener()

    }

    fun initView() {

        requestQueue = Volley.newRequestQueue(this)
        rl_listen.setOnClickListener(this)
        recycle_government.layoutManager = GridLayoutManager(this, 3)
        streetMainAdapter = StreetMainAdapter(this, strListGoverment, 1)
        recycle_government.adapter = streetMainAdapter

        recycle_party.layoutManager = GridLayoutManager(this, 3)
        streetMainAdapter = StreetMainAdapter(this, strListParty, 2)
        recycle_party.adapter = streetMainAdapter

        recycle_publicity.layoutManager = GridLayoutManager(this, 3)
        streetMainAdapter = StreetMainAdapter(this, strListPublicity, 2)
        recycle_publicity.adapter = streetMainAdapter


        recycle_for_people.layoutManager = GridLayoutManager(this, 3)
        streetMainAdapter = StreetMainAdapter(this, strListForPeople, 2)
        recycle_for_people.adapter = streetMainAdapter
        streetMainAdapter!!.onItemChildClickListener =
            BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->

                when (view.id) {
                    R.id.rl_bg -> {
                        when (strListForPeople[position]) {
                            "疫情信息" -> {
                                var intent = Intent(this, WebActivity::class.java)
                                intent.putExtra(
                                    "url",
                                    "https://news.ifeng.com/c/special/7uLj4F83Cqm"
                                )
                                startActivity(intent)
                            }
                            "通知公告" -> {
                             /*   var intent = Intent(this, StreetSceneryActivity::class.java)
                                startActivity(intent)*/
                                var intent =Intent(this,WebInfoActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    }
                }

            }

        recycle_gis.layoutManager = GridLayoutManager(this, 3)
        streetMainAdapter = StreetMainAdapter(this, strListGis, 2)
        recycle_gis.adapter = streetMainAdapter

        voiceDialog = VoiceDialog(this, R.style.CustomDialog, myDialogListener)

    }

    fun GetPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            val REQUEST_CODE_CONTACT = 101
            val permissions = arrayOf<String>(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            )
            //验证是否许可权限
            for (str in permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT)
                }
            }
        }
    }


    fun initHttpListener() {
        val stringRequest =
            JsonObjectRequest(Request.Method.GET, NetConstants.WeatherUrl,
                Response.Listener { response ->
                    Log.e("fhxx11", response.toString())
                    weatherNowBean = JSON.parseObject<WeatherNowBean>(
                        response.toString(),
                        WeatherNowBean::class.java
                    )
                    tv_tem.text = weatherNowBean!!.tem
                    tv_wea.text = "°C  ${weatherNowBean!!.wea}"
                    when (weatherNowBean!!.wea_img) {
                        "qing" -> {
                            image_wea.setImageResource(R.drawable.ic_qing)
                            image_little_wea.setImageResource(R.drawable.icon_qing)
                        }
                        "yu" -> {
                            image_wea.setImageResource(R.drawable.ic_yu)
                            image_little_wea.setImageResource(R.drawable.icon_yu)
                        }
                        "yin" -> {
                            image_wea.setImageResource(R.drawable.ic_yin)
                            image_little_wea.setImageResource(R.drawable.icon_yin)
                        }
                        "yun" -> {
                            image_wea.setImageResource(R.drawable.ic_yun)
                            image_little_wea.setImageResource(R.drawable.icon_yun)
                        }
                        "xue" -> {
                            image_wea.setImageResource(R.drawable.ic_xue)
                            image_little_wea.setImageResource(R.drawable.icon_bingbao)
                        }
                        "lei" -> {
                            image_wea.setImageResource(R.drawable.ic_lei)
                            image_little_wea.setImageResource(R.drawable.icon_lei)
                        }
                        "shachen" -> {
                            image_wea.setImageResource(R.drawable.ic_shachen)
                            image_little_wea.setImageResource(R.drawable.icon_shachen)
                        }
                        "wu" -> {
                            image_wea.setImageResource(R.drawable.ic_wu)
                            image_little_wea.setImageResource(R.drawable.icon_wu)
                        }
                        "bingbao" -> {
                            image_wea.setImageResource(R.drawable.ic_bingbao)
                            image_little_wea.setImageResource(R.drawable.icon_bingbao)
                        }
                    }


                }, Response.ErrorListener {

                })

        var jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            NetConstants.NowEpidemicUrl,
            Response.Listener { response ->
                var parseObject = JSON.parseObject(response.toString())
                var get = parseObject.get("data")

            },
            Response.ErrorListener {
            })

        requestQueue!!.add(stringRequest)
        requestQueue!!.add(jsonObjectRequest)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.rl_listen -> { //点击开始语音弹窗
                voiceDialog!!.show()
                var win = voiceDialog!!.window
                var attributes = win!!.attributes
                attributes.width = WindowManager.LayoutParams.MATCH_PARENT
                attributes.height = WindowManager.LayoutParams.MATCH_PARENT
                win.attributes = attributes
            }
        }
    }


    /**
     * 初始化监听器。
     */
    private val mInitListener = InitListener { code ->
        Log.d("TAG", "SpeechRecognizer init() code = $code")
        if (code != ErrorCode.SUCCESS) {
            Toast.makeText(
                this,
                "初始化失败，错误码：$code",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /* fun PostTest(){
         var map = TreeMap<String,String>()
         map["name"] = "chaychan"
         map["age"] = "22 years old"
         map["hobby"] = "programming";
         HttpUtil.getInstance().request(this,url,map,object :HttpCallBack<>)
     }*/

    private val myDialogListener: VoiceDialog.MyDialogListener =
        VoiceDialog.MyDialogListener { view ->
            when (view!!.id) {
                R.id.image_start -> {
                    Log.e("fhxx", "点击了start")
                    mIat!!.setParameter(SpeechConstant.RESULT_TYPE, "json")
                    mIat!!.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD)
                    mIat!!.setParameter(SpeechConstant.ACCENT, "mandarin")
                    mIat!!.setParameter(SpeechConstant.ASR_PTT, "0")
                    mIat!!.startListening(mRecognizerListener)
                }
                R.id.viewRight,
                R.id.viewTop,
                R.id.viewLeft,
                R.id.viewBottom -> {
                    voiceDialog!!.dismiss();
                }


            }
        }

    // 用HashMap存储听写结果
    private var mIatResults: HashMap<String, String> = LinkedHashMap<String, String>()
    private val mRecognizerListener: RecognizerListener = object : RecognizerListener {
        override fun onVolumeChanged(i: Int, bytes: ByteArray?) {
            Log.d("fhxx", "返回音频数据：" + i + "-----------" + bytes!!.size)
        }

        override fun onResult(results: RecognizerResult?, isLast: Boolean) {
            var result = results!!.getResultString(); //为解析的
            Log.e("fhxx", " 没有解析的 :" + result)

            var text = JsonParser.parseIatResult(result);//解析过后的
            Log.e("fhxx", " 解析后的 :" + text)
            var sn: String? = null

            // 读取json结果中的 sn字段

            var jsonObject = JSONObject(results.getResultString())
            sn = jsonObject.optString("sn");

            mIatResults.put(sn, text);//没有得到一句，添加到

            var stringBuffer = StringBuffer();
            for (key: String in mIatResults.keys) {
                stringBuffer.append(mIatResults[key])
            }
            etMessage.setText(stringBuffer.toString())
            //获取焦点
            etMessage.requestFocus();
            //将光标定位到文字最后，以便修改
            etMessage.setSelection(stringBuffer.length);
            Log.e("fhxx", "--->最新" + stringBuffer.toString())
            if (isLast){
                EventBus.getDefault().post(VoiceReplyBean(stringBuffer.toString(),1))
            }

        }

        override fun onBeginOfSpeech() {
            Log.d("fhxx", "开始说话")
            ToastUtils.show("开始说话")
        }

        override fun onEvent(eventType: Int, arg1: Int, arg2: Int, obj: Bundle?) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //
        }

        override fun onEndOfSpeech() {
            Log.d("fhxx", "结束说话")
            ToastUtils.show("结束说话")
        }

        override fun onError(speechError: SpeechError?) {
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。

            Log.d("fhxx", speechError!!.getPlainDescription(true))
        }

    }


}
