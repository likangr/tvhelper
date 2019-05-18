package com.likang.tvhelper.lib.util

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebView
import android.widget.GridView
import androidx.annotation.ColorInt

object ScreenUtil {

    const val MODE_ADAPT_TWO_SIDE = 1
    const val MODE_FORCE_ADAPT_SHORT_SIDE = 2
    const val MODE_FORCE_ADAPT_LONG_SIDE = 3

    /**
     * 屏幕高度
     */
    @JvmStatic
    var height = 0
    /**
     *屏幕宽度
     */
    @JvmStatic
    var width = 0

    private var statusHeight = -1

    private var systemDensityRatio = 0f
    private var adaptDensityRatio = 0f
    private lateinit var displayMetrics: DisplayMetrics
    private var webViewHasDestroyed = false
    private var mIsSetFontSizeToDefault = false

    @JvmField
    var IS_TOUCH_INTERACTION_MODE = hasTouchScreen()

    /**
     * 最小宽度适配 phone、tablet、tv；
     * 1.实现了与设计稿不同比例设备的兼容; e.g. 设计比例：16：9  设备比例 4：3  按照16:9显示 但是直接造成缺陷：不能充分使用屏幕像素;
     * 2.支持minSdkVersion>=17;
     *
     * @param application [or [android.app.Application]'s subClass]
     */
    @JvmStatic
    fun adaptDensity(application: Application, shortSideLengthWidthDp: Int, longSideLengthWithDp: Int, isSetFontSizeToDefault: Boolean, adaptMode: Int) {
        mIsSetFontSizeToDefault = isSetFontSizeToDefault
        getStatusHeight(application)
        application.registerComponentCallbacks(object : ComponentCallbacks {
            override fun onConfigurationChanged(newConfig: Configuration) {
                Logger.d("likang", "onConfigurationChanged")
                updateConfig(application, application.resources)
            }

            override fun onLowMemory() {

            }
        })
        if (Build.VERSION.SDK_INT >= 26) {

            application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    val resources = activity.resources
                    updateConfig(activity, resources)
                    Logger.d("likang", "onActivityCreated activity=$activity")
                }

                override fun onActivityStarted(activity: Activity) {
                    val resources = activity.resources
                    updateConfig(activity, resources)
                    Logger.d("likang", "onActivityStarted activity=$activity")
                }

                override fun onActivityResumed(activity: Activity) {
                    Logger.d("likang", "onActivityResumed activity=$activity")
                }

                override fun onActivityPaused(activity: Activity) {
                    Logger.d("likang", "onActivityPaused activity=$activity")
                }

                override fun onActivityStopped(activity: Activity) {
                    Logger.d("likang", "onActivityStopped activity=$activity")
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                    Logger.d("likang", "onActivitySaveInstanceState activity=$activity")
                }

                override fun onActivityDestroyed(activity: Activity) {
                    Logger.d("likang", "onActivityDestroyed activity=$activity")
                }
            })
        }

        //设计稿宽高比
        val aspectRatio = longSideLengthWithDp / shortSideLengthWidthDp.toFloat()

        val resources = application.resources
        val configuration = resources.configuration

        displayMetrics = DisplayMetrics()
        val windowManager = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)

        //屏幕物理高 px
        height = displayMetrics.heightPixels
        //屏幕物理宽 px
        width = displayMetrics.widthPixels
        Logger.d("adaptDensity", "screenHeightWithPx =$height")
        Logger.d("adaptDensity", "screenWidthWithPx =$width")

        //屏幕最小宽度 px
        val screenSwWithPx: Int
        //屏幕长边 px
        val screenLongSideLengthWithPx: Int

        if (height > width) {
            screenSwWithPx = width
            screenLongSideLengthWithPx = height
        } else {
            screenSwWithPx = height
            screenLongSideLengthWithPx = width
        }

        Logger.d("adaptDensity", "screenSwWithPx =$screenSwWithPx")
        Logger.d("adaptDensity", "screenLongSideLengthWithPx =$screenLongSideLengthWithPx")

        //屏幕最小宽度 dp
        val screenSwWithDp = configuration.smallestScreenWidthDp
        //屏幕密度比
        systemDensityRatio = displayMetrics.density

        Logger.d("adaptDensity", "screenSwWithDp =$screenSwWithDp")
        Logger.d("adaptDensity", "systemDensityRatio =$systemDensityRatio")

        val adaptSwWithDp: Int
        val adaptLongSideLengthWithDp: Int

        adaptSwWithDp = screenSwWithDp
        adaptLongSideLengthWithDp = (adaptSwWithDp * aspectRatio).toInt()

        Logger.d("adaptDensity", "adaptSwWithDp =$adaptSwWithDp")
        Logger.d("adaptDensity", "adaptLongSideLengthWithDp =$adaptLongSideLengthWithDp")

        if (adaptMode == MODE_ADAPT_TWO_SIDE) {
            val ratio1 = systemDensityRatio
            val ratio2 = screenLongSideLengthWithPx / adaptLongSideLengthWithDp.toFloat()
            adaptDensityRatio = if (ratio1 > ratio2) ratio2 else ratio1
        } else if (adaptMode == MODE_FORCE_ADAPT_SHORT_SIDE) {
            adaptDensityRatio = systemDensityRatio
        } else if (adaptMode == MODE_FORCE_ADAPT_LONG_SIDE) {
            adaptDensityRatio = screenLongSideLengthWithPx / adaptLongSideLengthWithDp.toFloat()
        }

        adaptDensityRatio *= screenSwWithDp / shortSideLengthWidthDp.toFloat()
        updateConfig(application, resources)
    }

    @JvmStatic
    private fun destroyWebView(context: Context) {
        try {//Caused by android.webkit.WebViewFactory$MissingWebViewPackageException
            WebView(context).destroy()//see https://stackoverflow.com/questions/40398528/android-webview-language-changes-abruptly-on-android-n
            webViewHasDestroyed = true
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @JvmStatic
    fun updateConfig(context: Context, resources: Resources) {
        if (!webViewHasDestroyed) {
            destroyWebView(context)
        }
        val newConfig = resources.configuration
        newConfig.densityDpi = (adaptDensityRatio * DisplayMetrics.DENSITY_DEFAULT).toInt()
        if (mIsSetFontSizeToDefault) {
            newConfig.fontScale = 1f
        }
        resources.updateConfiguration(newConfig, displayMetrics)
        Logger.d("updateConfig", "update systemDensityRatio $systemDensityRatio ==> $adaptDensityRatio")
    }


    /**
     * dip2px
     *
     * @param context
     * @param dpValue
     * @return
     */
    @JvmStatic
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * px2dip
     *
     * @param context
     * @return
     */
    @JvmStatic
    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * px2sp
     *
     * @param context
     * @return
     */
    @JvmStatic
    fun px2sp(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.scaledDensity
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * sp2px
     *
     * @param spValue
     * @return
     */
    @JvmStatic
    fun sp2px(context: Context, spValue: Float): Int {
        val scale = context.resources.displayMetrics.scaledDensity
        return (spValue * scale + 0.5f).toInt()
    }

    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    @JvmStatic
    fun getStatusHeight(context: Context): Int {
        if (statusHeight == -1) {
            try {
                val clazz = Class.forName("com.android.internal.R\$dimen")
                val height = Integer.parseInt(clazz.getField("status_bar_height")
                        .get(null).toString())
                statusHeight = context.resources.getDimensionPixelSize(height)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return statusHeight
    }

    /**
     * 设置view margin
     *
     * @param v
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @JvmStatic
    fun setMargins(v: View, l: Int, t: Int, r: Int, b: Int) {
        if (v.layoutParams is ViewGroup.MarginLayoutParams) {
            val p = v.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(l, t, r, b)
            v.requestLayout()
        }
    }

    @JvmStatic
    fun getAverageGridViewItemWidth(gridView: GridView, columns: Int, totalWidth: Int): Int {
        Logger.d("getAverageGridViewItemWidth", "gridView.requestedHorizontalSpacing=${gridView.requestedHorizontalSpacing}")
        Logger.d("getAverageGridViewItemWidth", "gridView.paddingLeft=${gridView.paddingLeft}")
        Logger.d("getAverageGridViewItemWidth", "gridView.paddingRight=${gridView.paddingRight}")
        return (totalWidth - (gridView.requestedHorizontalSpacing * (columns - 1) + gridView.paddingLeft + gridView.paddingRight)) / columns
    }

    @JvmStatic
    fun hideNavigationBar(activity: Activity) {
        if (Build.VERSION.SDK_INT < 19) { // lower api
            val v = activity.window.decorView
            v.systemUiVisibility = View.GONE
        } else {
            //for new api versions.
            val decorView = activity.window.decorView
            val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN)
            decorView.systemUiVisibility = uiOptions
        }
    }

    @JvmStatic
    fun hideStatusBar(activity: Activity) {
        val decorView = activity.window.decorView
        val option = View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = option
    }

    @JvmStatic
    fun setWindowStatusBarColor(activity: Activity, @ColorInt colorResId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = colorResId

        }

    }

    @JvmStatic
    fun hasTouchScreen(): Boolean {
        val packageManager = ApplicationHolder.getApplication().packageManager
        return packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
                || packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH)
    }

}