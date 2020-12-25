//package top.laoshuzi.signatureview
//
//import android.graphics.Bitmap
//import android.os.Bundle
//import android.view.View
//import android.view.Window
//import com.blankj.utilcode.util.ActivityUtils
//import com.blankj.utilcode.util.ScreenUtils
//import kotlinx.android.synthetic.main.fragment_patient_sign.*
//import top.laoshuzi.medicalbeauty.R
//import top.laoshuzi.medicalbeauty.common.ui.dialog.BaseDialogFragment
//import top.laoshuzi.medicalbeauty.common.ui.dialog.DialogLayoutCallback
//
//class SignatureDialog : BaseDialogFragment() {
//
//    var signHandler: ((bmp: Bitmap?) -> Unit)? = null
//
//    init {
//        val topActivity = ActivityUtils.getTopActivity()
//        super.init(topActivity, object : DialogLayoutCallback {
//            override fun bindTheme(): Int {
//                return R.style.CommonContentDialogStyle
//            }
//
//            override fun bindLayout(): Int {
//                return R.layout.fragment_patient_sign
//            }
//
//            override fun initView(dialog: BaseDialogFragment, contentView: View) {
//                val fillHeight = ScreenUtils.getAppScreenHeight()
//                val fillWidth = ScreenUtils.getAppScreenWidth()
//                val lp = view_sign.layoutParams
//                lp.width = (fillWidth * 0.85).toInt()
//                lp.height = (fillHeight * 0.65).toInt()
//                view_sign.layoutParams = lp
//
//                action_sign_clear.setOnClickListener {
//                    view_sign.clearCanvas()
//                }
//                action_sign_cancel.setOnClickListener {
//                    dismiss()
//                }
//                action_sign_ok.setOnClickListener {
//                    signHandler?.invoke(view_sign.getSignatureBitmap())
//                    dismiss()
//                }
//            }
//
//            override fun setWindowStyle(window: Window) {}
//            override fun onCancel(dialog: BaseDialogFragment) {}
//            override fun onDismiss(dialog: BaseDialogFragment) {}
//        })
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        dialog?.run {
//            setCanceledOnTouchOutside(false)
//        }
////        dialog?.window?.run {
////            val lp = attributes
////            lp.width = WindowManager.LayoutParams.MATCH_PARENT
////            lp.height = WindowManager.LayoutParams.MATCH_PARENT
////            attributes = lp
////        }
//    }
//
//}