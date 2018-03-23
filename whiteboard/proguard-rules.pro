# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\DevTools\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-dontoptimize
-ignorewarnings

-dontwarn android.support.**
-dontwarn com.actionbarsherlock.**
-dontwarn org.apache.**
-dontwarn groovy.**
-dontwarn org.codehaus.groovy.**
-dontwarn org.apache.commons.logging.**
-dontwarn java.lang.invoke.**

-dontnote com.google.vending.**
-dontnote com.android.vending.licensing.**

#-dontwarn com.protruly.whiteboard.MainActivity

-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# 四大组件相关
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * extends com.android.internal.telephony.ISms.Stub

# 针对android-support-*.jar的解决方案
-keep class android.support.**  { *; }
-keep interface android.support.** { *; }
-keep public class * extends android.support.**
-keep public class * extends android.app.Fragment

#保留泛型和内部类
-keepattributes Signature,InnerClasses
#抛出异常时保留代码行号，在异常分析中可以方便定位
-keepattributes SourceFile,LineNumberTable
#注解不混淆
-keepattributes *Annotation*
#内部类
-keepattributes EnclosingMethod

#保持本地方法
-keepclasseswithmembernames class * {
    native <methods>;
}

#自定义的组件不混淆
-keep public class * extends android.widget.Gallery
-keep public class * extends android.widget.ProgressBar
-keep public class * extends android.app.Dialog
-keep public class * extends android.widget.TextView
-keep public class * extends android.app.DialogFragment
-keep public class * extends android.widget.ListView
-keep public class * extends android.widget.ImageView
-keep public class * extends android.view.View

#过滤R文件的混淆
-keep class **.R$* {*;}
-keep class com.protruly.whiteboard.R$*{*;}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Serializable 的配置
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep public class * implements java.io.Serializable {*;}

# webview
-keepclassmembers class com.**.Activity$AppAndroid {
  public *;
}

-keepattributes *JavascriptInterface*

#反射有关的类不能混淆（发送PDU短信有关）
-keep class com.android.internal.telephony.IccSmsInterfaceManager { *; }
-keep class com.android.internal.telephony.Phone { *; }
-keep class com.android.internal.telephony.PhoneFactory { *; }
-keep class com.android.internal.telephony.RIL{ *; }
-keep class com.android.internal.telephony.RILConstants { *; }
-keep class com.android.internal.telephony.SMSDispatcher { *; }
-keep class com.android.internal.telephony.SmsRawData { *; }
-keep class com.android.internal.telephony.ISms { *; }
-keep class com.android.internal.telephony.ITelephony { *; }
-keep public class * extends android.os.Binder { *; }
-keep public class * implements com.android.internal.telephony.ISms{ *; }

-keep public class * extends android.**.Fragment { *; }
-keep class * extends android.os.AsyncTask { *; }

## 第三方库
# alipay支付
-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}
-keep class com.alipay.mobilesecuritysdk.*
-dontwarn com.alipay.apmobilesecuritysdk.face**
-keep class com.alipay.apmobilesecuritysdk.face.**{*;}
-keep class com.ut.*

# 微信
-keep class com.tencent.mm.sdk.openapi.WXMediaMessage {*;}
-keep class com.tencent.mm.sdk.openapi.** implements com.tencent.mm.sdk.openapi.WXMediaMessage$IMediaObject {*;}

# butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

## 网络请求okhttp
# http client
-keep class org.apache.http.** {*; }
-keep class org.apache.commons.lang.**{*;}

#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}

#okio
-dontwarn okio.**
-keep class okio.**{*;}
-dontwarn retrofit2.Platform$Java8

#okhttputils
-dontwarn com.zhy.http.**
-keep class com.zhy.http.**{*;}

# picasso
-keep class com.parse.*{ *; }
-dontwarn com.parse.**
-dontwarn com.squareup.picasso.**

# gson 有关JSON的类不混淆
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.examples.android.model.** { *; }

# 实体类不混淆
-keep class com.**.entity.**{*;}
-keep class com.**.bean.**{*;}

#百度地图
-keep class com.baidu.** { *; }
-keep class com.baidu.mapapi.** { *; }
-keep class vi.com.gdi.bgl.android.**{*;}
-keep class org.achartengine.** { *; }
-dontwarn com.baidu.**

#Google map
-dontwarn com.google.android.gsf.**
-keep class com.google.android.gsf.**{*; }

#反射引用资源文件
-keep class com.protruly.**.IDHelper{*; }
-keep class com.protruly.**.ResourceHelper{*; }

#android 6.X动态权限
-dontwarn com.protruly.permissions.**
-keep class com.protruly.permissions.**{*;}

#数据库litepal
-keep class org.litepal.** {
    *;
}

-keep class * extends org.litepal.crud.DataSupport {
    *;
}