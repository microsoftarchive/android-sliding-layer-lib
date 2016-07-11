6Wunderkinder SlidingLayer for Android
=============================
This repository hosts a library that provides an easy way to include an autonomous layer/view that slides from any side of your screen and which is fully gesture ready, the same way as our detail view in Wunderlist 2 does.
This pattern can also be seen in Google+’s notification center, Google Maps, Google Music, Contacts app in Lollipop, Basecamp’s detail view, among others.

If you want to see how it works you can have a look to our [video](http://www.youtube.com/watch?v=162oD0XPM40) or directly download from [Google Play](https://play.google.com/store/apps/details?id=com.slidinglayersample) it to test in on your device.


Implementation setup
------------------------------
As easy as to draw a green droid yourself, just grab it in your build gradle:

```gradle
compile 'com.wunderlist:sliding-layer:1.2.5'
```

or directly add it as a submodule inside of your project.


Integration
---------------
Due to simplicity and lightness, this container is currently based on a FrameLayout. Just treat it as you would with other container: Place it in any of your XML layout files or drag it from the Custom Components panel. Additionally you can add this view programmatically.
In the following example the same layout will be added by using the two mentioned ways.


XML
-----
```xml
<com.wunderlist.slidinglayer.SlidingLayer
    xmlns:slidingLayer="http://schemas.android.com/apk/res-auto"
    android:id="@+id/slidingLayer1"
    android:layout_width="@dimen/layer_width"
    android:layout_height="@dimen/layer_height"
    slidingLayer:shadowDrawable="@drawable/sidebar_shadow"
    slidingLayer:shadowSize="@dimen/shadow_size"
    slidingLayer:offsetDistance="@dimen/offset_distance"
    slidingLayer:previewOffsetDistance="@dimen/preview_offset_distance"
    slidingLayer:stickTo="top|bottom|right|left"
    slidingLayer:changeStateOnTap="true">

    …
    …
</com.wunderlist.slidinglayer.SlidingLayer>
```

Properties:
* `shadowDrawable` - a reference to the resource drawable used to paint the shadow of the container
* `shadowSize` - a reference to the dimension of the desired size of the given shadow
* `offsetDistance` - a reference to the dimension of the desired size for the layer to offset in the screen in order for it to be directly swipable to open
* `previewOffsetDistance` - a reference to the dimension of the desired size of the preview mode. When opening the layer in this state, it will only show this amount of the layer. Preview mode can be open by tapping the layer, dragging or programmatically by calling `openPreview(boolean smoothAnimation)`.
* `stickTo` - an enum that determines to where the container should stick to. ‘left’ sticks the container to the left side of the screen. ‘right’ sticks the container to the right side of the screen, and so on with ‘top‘ and ‘bottom‘ states. Default is ‘right’.
* `changeStateOnTap` - a boolean that enables/disables the action to change the state of the layer -open, preview or close- by tapping on an empty space of the container. Default value is true.


Java
-----
```java
public class SlidingLayerExampleActivity extends Activity {
  
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
  		setContentView(R.layout.main_view);
  
  		SlidingLayer slidingLayer = (SlidingLayer) findViewById(R.id.slidingLayer1);
      
        slidingLayer.setShadowDrawable(R.drawable.sidebar_shadow);
        slidingLayer.setShadowSizeRes(R.dimen.shadow_size);
        slidingLayer.setOffsetDistanceRes(R.dimen.offset_distance);
        slidingLayer.setPreviewOffsetDistanceRes(R.dimen.preview_offset_distance);
        slidingLayer.setStickTo(SlidingLayer.STICK_TO_LEFT);
        slidingLayer.setChangeStateOnTap(false);
  		
        slidingLayer.addView(new Button(this));
        ...
    }
}
```
Code of Conduct
-------------------

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.
