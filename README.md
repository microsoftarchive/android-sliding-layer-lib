6Wunderkinder SlidingLayer for Android
=============================
This repository host a library that provides an easy way to include an autonomous layer/view that slides from the side of your screen and which is fully gesture ready, the same way as our detail view in Wunderlist 2 does.
This pattern can also be seen in Google+’s notification center or in Basecamp’s detail view.

If you want to see how it works you can have a look to our [video](http://www.youtube.com/watch?v=162oD0XPM40) or directly download from [Google Play](https://play.google.com/store/apps/details?id=com.slidinglayersample) it to test in on your device.


Implementation setup
------------------------------
Easy as to draw a green droid yourself, just include the library inside of your source code and you are ready to go.


Integration
---------------
Due to simplicity and lightness, this container is currently based on a FrameLayout. Just treat it as you would with other container: Place it in any of your XML layout files or drag it from the Custom Components panel. Additionally you can add this view programmatically.
In the following example the same layout will be added by using the two mentioned ways.


XML
-----
```xml
<com.slidinglayer.SlidingLayer
    xmlns:slidingLayer="http://schemas.android.com/apk/res-auto"
    android:id="@+id/slidingLayer1"
    android:layout_width="@dimen/layer_width"
    android:layout_height="match_parent"
    slidingLayer:shadowDrawable="@drawable/sidebar_shadow"
    slidingLayer:shadowWidth="@dimen/shadow_width"
    slidingLayer:offsetWidth="@dimen/offset_width"
    slidingLayer:stickTo="auto|right|left|middle|top|bottom"
    slidingLayer:closeOnTapEnabled="true">

    …
    …
</com.slidinglayer.SlidingLayer>
```

Properties:
* `shadowDrawable` - a reference to the resource drawable used to paint the shadow of the container
* `shadowWidth` - a reference to the dimension of the desired width of the given shadow
* `offsetWidth` - a reference to the dimension of the desired width for the layer to offset in the screen in order for it to be directly swipable to open
* `stickTo` - an enum that determines to where the container should stick to. ‘left’ sticks the container to the left side of the screen. ‘right’ sticks the container to the right side of the screen, and so on with ‘top‘ and ‘bottom‘ states. ‘middle’ makes the container be centered covering the whole screen and ‘auto’ makes the decision based on where you deliberately placed your container in the view. Default is ‘auto’.
* `closeOnTapEnabled` - a boolean that enables/disables the action to close the layer by tapping on an empty space of the container. Default value is true.


Java
-----
```java
public class SlidingLayerExampleActivity extends Activity {
  
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
  		setContentView(R.layout.main_view);
  
  		SlidingLayer slidingLayer = (SlidingLayer) findViewById(R.id.slidingLayer1);
      
        slidingLayer.setShadowWidthRes(R.dimen.shadow_width);
        slidingLayer.setOffsetWidth(25);
        slidingLayer.setShadowDrawable(R.drawable.sidebar_shadow);
        slidingLayer.setStickTo(SlidingLayer.STICK_TO_LEFT);
        slidingLayer.setCloseOnTapEnabled(false);
  		
        slidingLayer.addView(new Button(this));
  
    }
}
```
