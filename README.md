![image](http://upload-images.jianshu.io/upload_images/3776310-98c87360fd3fbc32.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
# 需求由来
最近项目中来了新的需求，一大堆卡片式布局，还有不同的阴影颜色，甚至不同的状态下颜色还不一样，UI 给的切图各种错位，而 Google 的 **CardView** 是无法设置阴影颜色的，我能怎么办，我也很绝望啊_(:з」∠)_
没办法，百度了一堆都没有找到解决方案，最后借鉴了各位大神的思路，才有了这篇文章。

# 实现思路
怎么实现？当然是参照 Google 的 **CardView** 啦，毕竟是亲生的，各种实现优化都是非常棒的，然后就是在 **CardView** 的基础上，给他添加上设置阴影颜色的功能，很简单的是吧。

# 开始干活啦
* 那么先新建一个项目，然后把 **CardView** 目录下的所有 **类文件、资源文件** 都复制到新建的项目中。
![屏幕快照 2018-10-10 10.19.58.png](https://upload-images.jianshu.io/upload_images/3776310-df20619f72ebb5a6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

* 这样，这个 **CardView** 就已经可以使用了，和原生的一模一样，同样不能设置阴影颜色，这当然不是我们需要的。
* 我们的目标是让 **CardView** 能够支持设置阴影颜色，那么首先我们就来探查一下 **CardView** 的颜色资源文件。
![屏幕快照 2018-10-10 10.23.16.png](https://upload-images.jianshu.io/upload_images/3776310-77e51bd65215d962.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
```xml
    <color name="cardview_shadow_end_color">#03000000</color>
    <color name="cardview_shadow_start_color">#37000000</color>
```
* 这是什么？阴影的开始颜色和结束颜色？这么简单就解决了吗？我们可以尝试修改这两个颜色值，然而，并没有效果(O_O)? 这是为什么？
* 我们来查看这两个资源在哪些地方被使用。
![屏幕快照 2018-10-10 10.30.23.png](https://upload-images.jianshu.io/upload_images/3776310-db70e455646924f7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

* 这个 **RoundRectDrawableWithShadow** 是一个集成 **Drawable** 类，我们在查看一下这个类在哪里被使用。
![屏幕快照 2018-10-10 10.35.53.png](https://upload-images.jianshu.io/upload_images/3776310-945e3de2f343da21.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

* 他在 **CardViewBaseImpl** 中被创建，和这个类似的还有 **CardViewApi17Impl** 和 **CardViewApi21Impl**，其中 **CardViewApi17Impl** 是继承 **CardViewBaseImpl** 的，我们接下来再看看这两个类是在哪里使用的。
![屏幕快照 2018-10-10 10.39.21.png](https://upload-images.jianshu.io/upload_images/3776310-cea5e6d20234f217.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

* 我们在 **CardView** 中发现了他，嗯、、好像是 API 小于21才会使用，我们用一个API 19的模拟器试试。
![屏幕快照 2018-10-10 10.45.11.png](https://upload-images.jianshu.io/upload_images/3776310-06988ad6d3d5ca59.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

* 我们成功了，阴影的颜色被改变了，看样子之前之所以没有效果是因为模拟器的API大于等于21了，那么在高版本上我们改如何实现这种效果呢？
* 首先，我们来看一下，使用代码设置 **CardView** 高度时进行的操作。
```java
	public void setCardElevation(float elevation) {
        IMPL.setElevation(mCardViewDelegate, elevation);
    }
```
* 从上面的步骤我们可以知道，当 API 低于21时，**CardView** 使用  **CardViewBaseImpl** 和 **CardViewApi17Impl** 来处理的，高于或等于21时使用 **CardViewApi21Impl** 来处理。这里面有什么区别呢？
```java
	// CardViewBaseImpl
	@Override
    public void setElevation(CardViewDelegate cardView, float elevation) {
        getShadowBackground(cardView).setShadowSize(elevation);
    }
	// CardViewApi21Impl
	@Override
    public void setElevation(CardViewDelegate cardView, float elevation) {
        cardView.getCardView().setElevation(elevation);
    }
```
* 我们可以看到，当 API 高于或等于21时，使用的是从API21开始才有的Elevation属性设置阴影效果的，而低于21时是通过Drawable来绘制阴影效果。弄清楚了这些，我们就可以开始给 **CardView** 的阴影添加颜色啦。

# 实现可设置阴影颜色的 CardView
* 首先，我们需要给 **CardView** 添加两条属性，用来设置阴影的开始颜色和结束颜色。
```xml
	<attr name="cardShadowColorStart" format="color" />
    <attr name="cardShadowColorEnd" format="color" />
```
* 然后，在 **CardView** 的构造方法里面获取属性。
```java
	ColorStateList shadowColorStart = a.getColorStateList(R.styleable.CardView_cardShadowColorStart);
	ColorStateList shaodwColorEnd = a.getColorStateList(R.styleable.CardView_cardShadowColorEnd);
```
* 这里为了支持状态选择器，使用 **ColorStateList**
* **CardView** 的构造方法最后会调用 **CardViewImpl** 的 *initialize()* 方法进行初始化，因此在 *initialize()* 方法中添加 两个参数。
```java
void initialize(CardViewDelegate cardView, Context context, ColorStateList backgroundColor,
                    float radius, float elevation, float maxElevation, ColorStateList shadowColorStart, ColorStateList shadowColorEnd);
```
* 修改 **RoundRectDrawableWithShadow** 构造方法，添加阴影颜色参数，并进行处理。
```java
	// 阴影颜色默认是 int 类型的颜色值，要修改成 ColorStateList
	private final ColorStateList mShadowStartColor;

    private final ColorStateList mShadowEndColor;
    
	RoundRectDrawableWithShadow(Resources resources, ColorStateList backgroundColor, float radius,
                                float shadowSize, float maxShadowSize, ColorStateList shadowColorStart, ColorStateList shadowColorEnd) {
        // 如果没有设置阴影颜色，使用默认颜色
        if (shadowColorStart == null) {
            mShadowStartColor = ColorStateList.valueOf(resources.getColor(R.color.cardview_shadow_start_color));
        } else {
            mShadowStartColor = shadowColorStart;
        }
        if (shadowColorEnd == null) {
            mShadowEndColor = ColorStateList.valueOf(resources.getColor(R.color.cardview_shadow_end_color));
        } else {
            mShadowEndColor = shadowColorEnd;
        }
        mInsetShadow = resources.getDimensionPixelSize(R.dimen.cardview_compat_inset_shadow);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        setBackground(backgroundColor);
        mCornerShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mCornerShadowPaint.setStyle(Paint.Style.FILL);
        mCornerRadius = (int) (radius + .5f);
        mCardBounds = new RectF();
        mEdgeShadowPaint = new Paint(mCornerShadowPaint);
        mEdgeShadowPaint.setAntiAlias(false);
        setShadowSize(shadowSize, maxShadowSize);
    }
    
	private void buildShadowCorners() {
        RectF innerBounds = new RectF(-mCornerRadius, -mCornerRadius, mCornerRadius, mCornerRadius);
        RectF outerBounds = new RectF(innerBounds);
        outerBounds.inset(-mShadowSize, -mShadowSize);

        if (mCornerShadowPath == null) {
            mCornerShadowPath = new Path();
        } else {
            mCornerShadowPath.reset();
        }
        mCornerShadowPath.setFillType(Path.FillType.EVEN_ODD);
        mCornerShadowPath.moveTo(-mCornerRadius, 0);
        mCornerShadowPath.rLineTo(-mShadowSize, 0);
        // outer arc
        mCornerShadowPath.arcTo(outerBounds, 180f, 90f, false);
        // inner arc
        mCornerShadowPath.arcTo(innerBounds, 270f, -90f, false);
        mCornerShadowPath.close();
        float startRatio = mCornerRadius / (mCornerRadius + mShadowSize);
		
		// 获取当前状态下的阴影颜色
        int starColor = mShadowStartColor.getColorForState(getState(), mShadowStartColor.getDefaultColor());
        int endColor = mShadowEndColor.getColorForState(getState(), mShadowEndColor.getDefaultColor());
        // 设置阴影颜色
        mCornerShadowPaint.setShader(new RadialGradient(0, 0, mCornerRadius + mShadowSize,
                new int[]{starColor, starColor, endColor},
                new float[]{0f, startRatio, 1f},
                Shader.TileMode.CLAMP));

        // we offset the content shadowSize/2 pixels up to make it more realistic.
        // this is why edge shadow shader has some extra space
        // When drawing bottom edge shadow, we use that extra space.
        mEdgeShadowPaint.setShader(new LinearGradient(0, -mCornerRadius + mShadowSize, 0,
                -mCornerRadius - mShadowSize,
                new int[]{starColor, starColor, endColor},
                new float[]{0f, .5f, 1f}, Shader.TileMode.CLAMP));
        mEdgeShadowPaint.setAntiAlias(false);
    }
```
* 好了，这样就可以直接使用属性配置阴影颜色了，当然，还是只能在低版本使用。
```xml
	<cn.wj.android.colorcardview.CardView
        android:layout_width="200dp"
        android:layout_height="50dp"
        app:cardPreventCornerOverlap="true"
        app:cardBackgroundColor="#069ff1"
        app:cardShadowColorStart="#2dfd0000"
        app:cardShadowColorEnd="#03fd0000"
        app:cardUseCompatPadding="true"
        app:cardElevation="8dp" />
```
# 适配高版本
* 经过上面的步骤之后，我们的 **CardView** 已经可以通过属性设置阴影颜色了，同时支持状态选择器，但是，这些效果仅仅在API低于21时有效。那么接下来我们来处理高版本的阴影颜色。
* 我们知道，高版本中，**CardView** 的阴影效果是通过 **CardViewApi21Impl** 处理的，其内部是通过 API21 才加入的 Elevation 属性设置的，而 Elevation 这个属性，Google 并没有提供任何接口来对其阴影颜色进行设置。
* 我的解决思路是，既然高版本的 Elevation 没有提供方法，那么我们就使用低版本的方案处理。
![屏幕快照 2018-10-10 11.37.54.png](https://upload-images.jianshu.io/upload_images/3776310-13bc2f1ba18c4037.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
* 我们可以看到，**CardViewApi21Impl** 是直接实现了 **CardViewImpl** 接口的，而低版本的实现是在 **CardViewBaseImpl** 中的，**CardViewApi17Impl** 继承 **CardViewBaseImpl**，那么我们可以让 **CardViewApi21Impl** 直接继承 **CardViewApi17Impl**。
* 同时进行判断，如果自定义了阴影颜色，那么就是用父类的实现，即低版本的实现，否则，依旧使用高版本的实现。
```java
@RequiresApi(21)
class CardViewApi21Impl extends CardViewApi17Impl {

	// 标记 - 是否使用低版本实现
    private boolean useLower = false;

    @Override
    public void initialize(CardViewDelegate cardView, Context context,
                           ColorStateList backgroundColor, float radius, float elevation, float maxElevation,
                           ColorStateList shadowColorStart, ColorStateList shadowColorEnd) {

		// 没有自定义阴影颜色，不使用低版本实现
        if (shadowColorStart == null && shadowColorEnd == null) {
            useLower = false;

            final RoundRectDrawable background = new RoundRectDrawable(backgroundColor, radius);
            cardView.setCardBackground(background);

            View view = cardView.getCardView();
            view.setClipToOutline(true);
            view.setElevation(elevation);
            setMaxElevation(cardView, maxElevation);
        } else {
        	// 配置了自定义颜色，使用低版本实现
            useLower = true;
            super.initialize(cardView, context, backgroundColor, radius, elevation, maxElevation, shadowColorStart, shadowColorEnd);
        }
    }

    @Override
    public void setRadius(CardViewDelegate cardView, float radius) {
        if (useLower) {
            super.setRadius(cardView, radius);
        } else {
            getCardBackground(cardView).setRadius(radius);
        }
    }

    @Override
    public void setMaxElevation(CardViewDelegate cardView, float maxElevation) {
        if (useLower) {
            super.setMaxElevation(cardView, maxElevation);
        } else {
            getCardBackground(cardView).setPadding(maxElevation,
                    cardView.getUseCompatPadding(), cardView.getPreventCornerOverlap());
            updatePadding(cardView);
        }
    }

    @Override
    public float getMaxElevation(CardViewDelegate cardView) {
        if (useLower) {
            return super.getMaxElevation(cardView);
        } else {
            return getCardBackground(cardView).getPadding();
        }
    }

    @Override
    public float getMinWidth(CardViewDelegate cardView) {
        if (useLower) {
            return super.getMinWidth(cardView);
        } else {
            return getRadius(cardView) * 2;
        }
    }

    @Override
    public float getMinHeight(CardViewDelegate cardView) {
        if (useLower) {
            return super.getMinHeight(cardView);
        } else {
            return getRadius(cardView) * 2;
        }
    }

    @Override
    public float getRadius(CardViewDelegate cardView) {
        if (useLower) {
            return super.getRadius(cardView);
        } else {
            return getCardBackground(cardView).getRadius();
        }
    }

    @Override
    public void setElevation(CardViewDelegate cardView, float elevation) {
        if (useLower) {
            super.setElevation(cardView, elevation);
        } else {
            cardView.getCardView().setElevation(elevation);
        }
    }

    @Override
    public float getElevation(CardViewDelegate cardView) {
        if (useLower) {
            return super.getElevation(cardView);
        } else {
            return cardView.getCardView().getElevation();
        }
    }

    @Override
    public void updatePadding(CardViewDelegate cardView) {
        if (useLower) {
            super.updatePadding(cardView);
        } else {
            if (!cardView.getUseCompatPadding()) {
                cardView.setShadowPadding(0, 0, 0, 0);
                return;
            }
            float elevation = getMaxElevation(cardView);
            final float radius = getRadius(cardView);
            int hPadding = (int) Math.ceil(RoundRectDrawableWithShadow
                    .calculateHorizontalPadding(elevation, radius, cardView.getPreventCornerOverlap()));
            int vPadding = (int) Math.ceil(RoundRectDrawableWithShadow
                    .calculateVerticalPadding(elevation, radius, cardView.getPreventCornerOverlap()));
            cardView.setShadowPadding(hPadding, vPadding, hPadding, vPadding);
        }
    }

    @Override
    public void onCompatPaddingChanged(CardViewDelegate cardView) {
        if (useLower) {
            super.onCompatPaddingChanged(cardView);
        } else {
            setMaxElevation(cardView, getMaxElevation(cardView));
        }
    }

    @Override
    public void onPreventCornerOverlapChanged(CardViewDelegate cardView) {
        if (useLower) {
            super.onPreventCornerOverlapChanged(cardView);
        } else {
            setMaxElevation(cardView, getMaxElevation(cardView));
        }
    }

    @Override
    public void setBackgroundColor(CardViewDelegate cardView, @Nullable ColorStateList color) {
        if (useLower) {
            super.setBackgroundColor(cardView, color);
        } else {
            getCardBackground(cardView).setColor(color);
        }
    }

    @Override
    public ColorStateList getBackgroundColor(CardViewDelegate cardView) {
        if (useLower) {
            return super.getBackgroundColor(cardView);
        } else {
            return getCardBackground(cardView).getColor();
        }
    }

    private RoundRectDrawable getCardBackground(CardViewDelegate cardView) {
        return ((RoundRectDrawable) cardView.getCardBackground());
    }
}
```
* 到这里，可以设置阴影颜色的 **CardView** 就已经完成了，我们来看看效果
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:orientation="vertical"
    tools:context=".MainActivity">

    <cn.wj.android.colorcardview.CardView
        android:id="@+id/cv1"
        android:layout_width="200dp"
        android:layout_height="100dp"
        app:cardBackgroundColor="@color/app_selector_card"
        app:cardElevation="8dp"
        app:cardPreventCornerOverlap="true"
        app:cardShadowColorEnd="@color/app_selector_shadow_end"
        app:cardShadowColorStart="@color/app_selector_shadow_start"
        app:cardUseCompatPadding="true" />

    <android.support.v7.widget.CardView
        android:id="@+id/cv2"
        android:layout_width="200dp"
        android:layout_height="100dp"
        app:cardBackgroundColor="@color/app_selector_card"
        app:cardElevation="8dp"
        app:cardPreventCornerOverlap="true"
        app:cardUseCompatPadding="true" />

</LinearLayout>
```
```java
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cv1.setOnClickListener { onClick(it) }
        cv2.setOnClickListener { onClick(it) }
    }

    fun onClick(v: View) {
        v.isSelected = !v.isSelected
    }
}
```
![2018-10-10 14.49.23.gif](https://upload-images.jianshu.io/upload_images/3776310-102922c09375948a.gif?imageMogr2/auto-orient/strip)

# 最后
* 嗯嗯，就这样，一个能够自定义阴影颜色的 **CardView** 就完成了。
* 这里给上项目地址，所有代码都在这里 [MyCardView](https://github.com/Lorry0822/MyCardView)。