#多主题

##代码结构
1、多主题实现代码在multitheme模块；
2、多主题测试代码在app模块；

##集成方法:
1、在Application的onCreate中调用如下代码，初始化话多主题sdk
    MultiThemeSDK.getInstance().initial(app, mCurrThemeIndex);// mCurrThemeIndex表示当前主题index
2、切换主题的时候调用如下代码
   MultiThemeSDK.getInstance().refreshTheme(mCurrThemeIndex);// mCurrThemeIndex表示当前主题index

3、需要支持多主题的资源，名称命名前缀固定为theme_,
   每个主题名(如果是文件则不包含文件类型后缀)后缀加上主题index，
   比如文本字体颜色，
                   主题0,  theme_main_text_color.xml;
                   主题1, theme_main_text_color_1.xml;
                   主题2, theme_main_text_color_2.xml;

   比如View的背景颜色，
                       <color name="theme_main_bg">#ffdddddd</color>
                       <color name="theme_main_bg_1">#ffffffff</color>
                       <color name="theme_main_bg_2">#ff777777</color>

   比如ImageView的图片资源，
                      主题0,  theme_image.png;
                      主题1,  theme_image_1.png;
                      主题2,  theme_image_2.png;










