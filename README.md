# Jumping Show 跳跳秀
跳跳秀，是微信“跳一跳”Android端的辅助APP，无需连接电脑ADB，无需ROOT权限，利用Android的无障碍服务、MediaProjection和OpenCV完成。

辅助APP要求Android设备的系统为Android 7.0以上（无障碍服务模拟屏幕点按的功能添加于API 24）。

## 一、 效果展示
*视频待补充*
*图片待补充*

## 二、 操作栏说明
<img src="./raw/action_bar.jpg" width="290" height="94"/>

操作栏从左至右包含4个按钮，功能分别为：
* 自动/手动切换，自动模式下在跳跃完成后自动准备下一次跳跃，手动模式需要用户点击“跳”实现；
* 跳， 点击一次，辅助程序会检测是否授予录屏权限（用于截图），权限满足后，执行辅助跳跃；
* 设置，前往辅助APP设置界面；
* 退出，关闭辅助操作栏，结束辅助功能。再次调出操作栏，需前往APP设置。

## 三、 辅助思路
1. 无障碍服务在屏幕上显示悬浮辅助操作栏；
2. Android的MediaProjection功能完成游戏截图；
3. OpenCV的Imgproc.matchTemplate()方法匹配寻找游戏截图中的紫色小人儿；
4. OpenCV的Mat.submat()裁剪图片，得到着陆点区域，Imgproc.Canny()提取显示图片中物体边缘的黑白图，逐行扫描图片寻找物体边缘的最高点；
5. 根据起跳点、着陆点的连线与截图水平边、截图竖直边的三边关系，计算起跳点、着陆点的连线线段长度；
6. 根据跳跃系数转换跳跃长度为按压时间，无障碍服务模拟屏幕上按压，完成一次跳跃功能。

## 四、 参数说明

#### 基础设置
<img src="./raw/basic_settings.jpg" width="540" height="426"/>

1. 总开关： 处于关闭状态时，不会弹出辅助操作栏；
2. 辅助栏开启方式：手动打开，总开关开启后立即显示操作栏；通知打开，当手机界面出现“跳一跳”游戏入口，弹出打开操作栏通知，点击通知显示操作栏；关闭辅助，始终不显示操作栏；
3. 保存截图： 游戏过程中，保存截图，包括界面截图，下一跳区域截图，边缘图等，保存路径为`/sdcard/jumpingshow/`；
4. 启动时清除历史截图：避免生成的截图占用过多存储。 *待完成*

#### 起跳区参数
<img src="./raw/jump_area.jpg" width="540" height="265"/>

起跳区就是紫色小人儿所在的区域，起跳区参数用于精确定位起跳点坐标
1. 小瓶子缩放比：不同分辨率、大小的手机屏幕中的小人儿宽高也不一样，需要用户从屏幕截图中测量小人儿的宽度或高度，单位像素，基准宽高为66x178。
    比如小米MIX中截图的小人儿宽高为78x212，缩放比即为78/66=1.1818。
2. 底部中心百分比：定位到小人儿后，设定精确起跳点的横坐标为小人儿水平最宽处，纵坐标为竖直中点，该值应该是0.910112。

#### 着陆区参数
<img src="./raw/landing_area.jpg" width="540" height="359"/>

着陆区就是小人儿下一步要跳的区域，着陆区参数用于精确定位着陆点坐标
1. 得分Y坐标：游戏界面左上角显示当前用户得分，Y坐标表示分数区域底部距离屏幕顶部的长度，单位为像素。该参数用于切割游戏界面截图，排除分数区域对着陆区的影响；
2. 边缘检测低阈值：使用OpenCV提取着陆区的图像边缘参数threshold1，详情参见[OpenCV.Canny()](https://docs.opencv.org/3.4.1/dd/d1a/group__imgproc__feature.html#ga2a671611e104c093843d7b7fc46d24af);
3. 边缘检测高阈值：使用OpenCv的Canny（）方法提取着陆区的图像边缘参数threshold3，参见[OpenCV.Canny()](https://docs.opencv.org/3.4.1/dd/d1a/group__imgproc__feature.html#ga2a671611e104c093843d7b7fc46d24af);

#### 距离参数
<img src="./raw/distance_params.jpg" width="540" height="451"/>

距离参数用于计算起跳点和着陆点之间的距离，并转换为按压时间
1. 跳跃系数：屏幕上的跳跃距离（单位：像素）与按压时间的系数；
2. 停留时间：跳跃到着陆点之后，游戏界面会有波纹特效，因此需要停留一段时间保证后续截图是静态的游戏画面；
3. 竖直边/水平边：起跳点与着陆点的连线与屏幕截图的水平边、竖直边形成三角形。该参数为竖直边长度与水平边长度的壁纸，为0.58；
4. 斜边/水平边：起跳点与着陆点的连线与屏幕截图的水平边、竖直边形成三角形。该参数为斜边长度与水平边长度的比值，为1.156；
    显然，水平长度为基准1的时候，以上2个参数应该满足勾股定理：1^2 + 0.58^2 = 1.156^2。

## 五、 后续改进
* 辅助操作栏启动时，不会删除历史截图（还没写相关代码）；
* 第一次点击跳跃按钮时，需要申请录制屏幕权限，然后3秒后手动点击第二次才会开始跳跃；
* 显示辅助操作栏的时候前往修改参数，需要关闭操作栏再打开，修改后的参数才会生效；
* 两次跳跃间隔时间太短会导致截图失败，所以会停留较长时间（3s左右）；
* 后续通过申请ROOT权限降低辅助APP对Android系统的要求；

## 六、 参考资料
* Accessibility: [Developing an Accessibility Service for Android](https://codelabs.developers.google.com/codelabs/developing-android-a11y-service/index.html#0)
* MediaProjection: [Google sample, android-ScreenCapture](https://github.com/googlesamples/android-screenCapture)
* OpenCV: [OpenCV 3.4.1 Documentation](https://docs.opencv.org/3.4.1/)