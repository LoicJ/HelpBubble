
package fr.jacques.loic.helpbubble;

		import android.content.Context;
		import android.graphics.Bitmap;
		import android.graphics.Canvas;
		import android.graphics.Color;
		import android.graphics.Paint;
		import android.graphics.PorterDuff;
		import android.graphics.PorterDuffXfermode;
		import android.graphics.Rect;
		import android.graphics.Region;
		import android.graphics.Typeface;
		import android.util.AttributeSet;
		import android.view.View;
		import android.view.animation.AlphaAnimation;
		import android.view.animation.Animation;
		import android.widget.ImageView;
		import android.widget.RelativeLayout;
		import android.widget.TextView;

/**
 * Created by loic on 03/03/2014.
 */
public class HelpBubble extends RelativeLayout {

	private static final int HELP_TEXT_ID = 100;
	private static final int HELP_IMAGE_ID = 101;

	private static final int SPACE_BETWEEN_VIEW = 10;

	// Bubble attribute
	public static enum BubbleType {
		WithHole,
		WithoutHole;
	}

	private BubbleType bubbleType;
	private boolean isVisible;

	// Image attributes
	public static enum ImagePosition {
		LeftOfHole,
		TopOfHole,
		RightOfHole,
		BottomOfHole,
		BottomOfText,
		CenterInParent,
		CenterHorizontally,
		CenterVertically;
	}

	private ImagePosition imagePosition;
	private int offsetImageX;
	private int offsetImageY;
	private ImageView helpImage;
	private int widthImage;

	// Text attributes
	public static enum TextPosition {
		RightOfImage,
		BottomOfImage,
		CenterVertically,
		CenterInParent,
		CenterHorizontally,;
	}

	private TextPosition textPosition;
	private TextView helpText;
	private String fontName;
	private int offsetTextX;
	private int offsetTextY;

	// Hole attributes
	private boolean showBorderHole;
	private int borderHoleWidth;
	private int borderHoleColor;
	private View visibleView;
	private Rect visibleArea;
	private int offsetHoleX;
	private int offsetHoleY;

	// Draw hole attributes
	private Paint mPaint;
	private Paint eraser;
	private int color;
	private Rect canvasRect;
	private Bitmap b;
	private Canvas c;

	// Animation attributes
	private Animation showAnimation;
	private int animationDuration;

	private Context context;

	public HelpBubble(Context context) {
		super(context);
		if (!this.isInEditMode()) {
			this.context = context;
			init();
		}
	}

	public HelpBubble(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (!this.isInEditMode()) {
			this.context = context;
			init();
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (w != oldw || h != oldh) {
			b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			c = new Canvas(b);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (canvasRect == null) {
			canvasRect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
		}
		// If Bubble is a WithoutHoleBubble, only draw background overlay
		if (bubbleType == BubbleType.WithoutHole) {
			canvas.drawColor(color);
			canvas.clipRect(canvasRect, Region.Op.REPLACE);
		}
		// Else, draw background + hole
		else {
			if (visibleArea != null) {
				b.eraseColor(Color.TRANSPARENT);
				c.drawColor(color);
				c.drawRect(visibleArea, eraser);
				canvas.drawBitmap(b, 0, 0, null);

				if (showBorderHole) {
					canvas.drawRect(visibleArea, mPaint);
				}
			} else {
				canvas.drawColor(color);
				canvas.clipRect(canvasRect, Region.Op.REPLACE);
			}
		}
	}

	// **********************************
	//			Private Method
	// **********************************

	private void init() {
		// Initialize drawing method and attributes
		this.setWillNotDraw(false); // To force View redraw when invalidate

		borderHoleWidth = 6;
		borderHoleColor = Color.YELLOW;

		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(borderHoleColor);
		mPaint.setStrokeWidth(borderHoleWidth);
		eraser = new Paint();
		eraser.setColor(Color.TRANSPARENT);
		eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

		showBorderHole = true;

		// Parameters TextView
		helpText = new TextView(context);
		helpText.setTextColor(Color.WHITE);
		helpText.setId(HELP_TEXT_ID);
//		helpText.setGravity(Gravity.CENTER);
		setTypeface();
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		helpText.setLayoutParams(params);
		this.addView(helpText);

		// Parameters ImageView
		widthImage = 100;
		helpImage = new ImageView(context);
		params = new LayoutParams(LayoutParams.WRAP_CONTENT, widthImage);
		helpImage.setLayoutParams(params);
		helpImage.setAdjustViewBounds(true);
		helpImage.setId(HELP_IMAGE_ID);
		this.addView(helpImage);

		//Initialize Default fade int animation
		animationDuration = 250;
		showAnimation = fadeIn(this, animationDuration);

		// Initialize Default position
		imagePosition = ImagePosition.CenterInParent;
		offsetImageX = 0;
		offsetImageY = 0;
		offsetHoleX = 0;
		offsetHoleY = 0;
		offsetTextX = 0;
		offsetTextY = 0;
		textPosition = TextPosition.CenterHorizontally;

		hide();
	}

	private int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	private int getSizeInRealPixel(int dpSize) {
		float d = context.getResources().getDisplayMetrics().density;
		return (int) (dpSize * d); // margin in pixels
	}

	private Animation fadeIn(final View v, int mShortAnimationDuration) {

		// Set the content view to 0% opacity but visible, so that it is visible
		// (but fully transparent) during the animation.
		// v.setAlpha(0.0f);
		AlphaAnimation animAlphaIn = new AlphaAnimation(0.0f, 1.0f);
		animAlphaIn.setFillAfter(true);
		animAlphaIn.setDuration(mShortAnimationDuration);
		animAlphaIn.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				v.setVisibility(View.VISIBLE);
				v.setEnabled(false);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				v.clearAnimation();
				v.setEnabled(true);
			}
		});
		return animAlphaIn;
	}

	private void setTypeface() {
		if (fontName != null && !fontName.equals("")) {
			Typeface font = Typeface.createFromAsset(context.getAssets(), fontName);
			helpText.setTypeface(font);
			helpText.setTextSize(22);
		}
	}

	private void positionElements() {
		int holeHeight = 0;
		int holeWidth = 0;
		if (visibleArea != null) {
			holeWidth = visibleArea.right - visibleArea.left;
			holeHeight = visibleArea.bottom - visibleArea.top;
		}
		LayoutParams paramsImage = new LayoutParams(LayoutParams.WRAP_CONTENT, widthImage);
		if (helpImage.getDrawable() != null) {
			int i, j;
			i = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
			j = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
			helpImage.measure(i, j);

			if (imagePosition == ImagePosition.CenterInParent) {
				paramsImage.addRule(RelativeLayout.CENTER_IN_PARENT);
			} else if (imagePosition == ImagePosition.CenterHorizontally) {
				paramsImage.addRule(RelativeLayout.CENTER_HORIZONTAL);
			} else if (imagePosition == ImagePosition.CenterVertically) {
				paramsImage.addRule(RelativeLayout.CENTER_VERTICAL);
			} else if (imagePosition == ImagePosition.LeftOfHole) {
				paramsImage.addRule(ALIGN_PARENT_LEFT);
				paramsImage.addRule(ALIGN_PARENT_TOP);
				int offsetY = (holeHeight - paramsImage.height) / 2;
				paramsImage.setMargins(visibleArea.left - helpImage.getMeasuredWidth(), visibleArea.top + offsetY, 0, 0);
			} else if (imagePosition == ImagePosition.RightOfHole) {
				paramsImage.addRule(ALIGN_PARENT_LEFT);
				paramsImage.addRule(ALIGN_PARENT_TOP);
				int offsetY = (holeHeight - paramsImage.height) / 2;
				paramsImage.setMargins(visibleArea.right, visibleArea.top + offsetY, 0, 0);
			} else if (imagePosition == ImagePosition.TopOfHole) {
				paramsImage.addRule(ALIGN_PARENT_LEFT);
				paramsImage.addRule(ALIGN_PARENT_TOP);
				int offsetX = (holeWidth - helpImage.getMeasuredWidth()) / 2;
				paramsImage.setMargins(visibleArea.left + offsetX, visibleArea.top - paramsImage.height, 0, 0);
			} else if (imagePosition == ImagePosition.BottomOfHole) {
				paramsImage.addRule(ALIGN_PARENT_LEFT);
				paramsImage.addRule(ALIGN_PARENT_TOP);
				int offsetX = (holeWidth - helpImage.getMeasuredWidth()) / 2;
				paramsImage.setMargins(visibleArea.left + offsetX, visibleArea.top + holeHeight, 0, 0);
			} else if (imagePosition == ImagePosition.BottomOfText) {
				paramsImage.addRule(RelativeLayout.CENTER_HORIZONTAL);
				paramsImage.addRule(BELOW, helpText.getId());
			}
			paramsImage.leftMargin += offsetImageX;
			paramsImage.topMargin += offsetImageY;
//			paramsImage.rightMargin += SPACE_BETWEEN_VIEW;
//			paramsImage.bottomMargin += SPACE_BETWEEN_VIEW;

			helpImage.setLayoutParams(paramsImage);
		}

		LayoutParams paramsText = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		if (textPosition == TextPosition.CenterInParent) {
			paramsText.addRule(RelativeLayout.CENTER_IN_PARENT);
		} else if (textPosition == TextPosition.CenterHorizontally) {
			paramsText.addRule(RelativeLayout.CENTER_HORIZONTAL);
		} else if (textPosition == TextPosition.CenterVertically) {
			paramsText.addRule(RelativeLayout.CENTER_VERTICAL);
		} else if (textPosition == TextPosition.RightOfImage) {
			paramsText.addRule(RelativeLayout.CENTER_VERTICAL);
			paramsText.leftMargin = (paramsImage.leftMargin - helpText.getMeasuredWidth());
		} else if (textPosition == TextPosition.BottomOfImage) {
			paramsText.addRule(RelativeLayout.CENTER_HORIZONTAL);
			paramsText.addRule(BELOW, helpImage.getId());
			paramsText.addRule(RelativeLayout.ALIGN_LEFT, helpImage.getId());
			paramsText.addRule(ALIGN_PARENT_RIGHT);
		}
		paramsText.leftMargin += offsetTextX;
		paramsText.topMargin += offsetTextY;
//		paramsText.rightMargin += SPACE_BETWEEN_VIEW;
//		paramsText.bottomMargin += SPACE_BETWEEN_VIEW;
		helpText.setLayoutParams(paramsText);
	}

	private void createHoleArea() {
		if (visibleView != null) {
			int[] location = new int[2];
			visibleView.getLocationOnScreen(location);

			visibleArea = new Rect();
			visibleView.getLocalVisibleRect(visibleArea);
			location[1] -= getStatusBarHeight();
			visibleArea.left = (location[0] + visibleView.getPaddingLeft());
			visibleArea.top = (location[1] + visibleView.getPaddingTop());
			visibleArea.right += (location[0] - visibleView.getPaddingRight()) - offsetHoleX;
			visibleArea.bottom += (location[1] - visibleView.getPaddingBottom()) - offsetHoleY;
		}
	}

	// **********************************
	//			Public Method
	// **********************************

	//
	//	Method related to Hole Area
	//

	/**
	 * Set the View which must be seen in the hole
	 *
	 * @param visibleView A view already display in the screen
	 * @return bubble itself
	 */
	public HelpBubble setVisibleView(View visibleView) {
		this.visibleView = visibleView;
		return this;
	}

	/**
	 * Set the Visible Area coordinate of the hole
	 *
	 * @param visibleArea A Rect with coordinate left, top, right and bottom
	 * @return bubble itself
	 */
	public HelpBubble setVisibleArea(Rect visibleArea) {
		this.visibleArea = visibleArea;
		return this;
	}

	/**
	 * Set the offset of the position X of the Hole, if needed
	 *
	 * @param offsetHoleX int offset in pixel
	 * @return bubble itself
	 */
	public HelpBubble setOffsetHoleX(int offsetHoleX) {
		this.offsetHoleX = offsetHoleX;
		return this;
	}

	/**
	 * Set the offset of the position Y of the Hole, if needed
	 *
	 * @param offsetHoleY int offset in pixel
	 * @return bubble itself
	 */
	public HelpBubble setOffsetHoleY(int offsetHoleY) {
		this.offsetHoleY = offsetHoleY;
		return this;
	}

	//
	//	Method related to Bubble Apparence
	//

	/**
	 * Set the type of the Bubble
	 *
	 * @param bubbleType {@link fr.jacques.loic.helpbubble.HelpBubble.BubbleType}
	 * @return bubble itself
	 */
	public HelpBubble setBubbleType(BubbleType bubbleType) {
		this.bubbleType = bubbleType;
		return this;
	}

	/**
	 * Set the position of the Image
	 *
	 * @param imagePosition {@link fr.jacques.loic.helpbubble.HelpBubble.ImagePosition}
	 * @return bubble itself
	 */
	public HelpBubble setImagePosition(ImagePosition imagePosition) {
		this.imagePosition = imagePosition;
		return this;
	}

	/**
	 * Set the width of Image View
	 *
	 * @return bubble itself
	 */
	public HelpBubble setWidthImage(int widthImage) {
		this.widthImage = widthImage;
		return this;
	}

	/**
	 * Set the offset position X of the image, if needed
	 *
	 * @param offsetImageX int offset in pixel
	 * @return bubble itself
	 */
	public HelpBubble setOffsetImageX(int offsetImageX) {
		this.offsetImageX = offsetImageX;
		return this;
	}

	/**
	 * Set the offset position Y of the image, if needed
	 *
	 * @param offsetImageY int offset in pixel
	 * @return bubble itself
	 */
	public HelpBubble setOffsetImageY(int offsetImageY) {
		this.offsetImageY = offsetImageY;
		return this;
	}

	/**
	 * Set the position of the Text
	 *
	 * @param textPosition {@link fr.jacques.loic.helpbubble.HelpBubble.TextPosition}
	 * @return bubble itself
	 */
	public HelpBubble setTextPosition(TextPosition textPosition) {
		this.textPosition = textPosition;
		return this;
	}

	/**
	 * Set the string value of the TextView
	 *
	 * @return bubble itself
	 */
	public HelpBubble setHelpText(String text) {
		helpText.setText(text);
		return this;
	}

	/**
	 * Set the font of the textView
	 *
	 * @param fontName String name of the font, must include subfolder if needed
	 * @return bubble itself
	 */
	public HelpBubble setFontName(String fontName) {
		this.fontName = fontName;
		setTypeface();
		return this;
	}

	/**
	 * Set the Image display
	 *
	 * @param imageFileName String name of the drawable
	 * @return bubble itself
	 */
	public HelpBubble setHelpImageFileName(String imageFileName) {
		int imageId = context.getResources().getIdentifier(imageFileName, "drawable", context.getPackageName());
		if (imageId != 0) {
			helpImage.setImageResource(imageId);
		}
		return this;
	}

	/**
	 * Set the offset position X of the text, if needed
	 *
	 * @param offsetTextX int offset in pixel
	 * @return bubble itself
	 */
	public HelpBubble setOffsetTextX(int offsetTextX) {
		this.offsetTextX = offsetTextX;
		return this;
	}

	/**
	 * Set the offset position Y of the text, if needed
	 *
	 * @param offsetTextY int offset in pixel
	 * @return bubble itself
	 */
	public HelpBubble setOffsetTextY(int offsetTextY) {
		this.offsetTextY = offsetTextY;
		return this;
	}

	/**
	 * Set the color of the Overlay
	 *
	 * @return bubble itself
	 */
	public HelpBubble setColor(int color) {
		this.color = color;
		return this;
	}

	/**
	 * Set whether border must be draw around the hole
	 *
	 * @param showBorderHole true to display border
	 * @return bubble itself
	 */
	public HelpBubble setShowBorderHole(boolean showBorderHole) {
		this.showBorderHole = showBorderHole;
		return this;
	}

	/**
	 * If Hole's border is set, set its size
	 *
	 * @param borderHoleWidth int size in pixel
	 * @return bubble itself
	 */
	public HelpBubble setBorderHoleWidth(int borderHoleWidth) {
		this.borderHoleWidth = borderHoleWidth;
		mPaint.setStrokeWidth(borderHoleWidth);
		return this;
	}

	/**
	 * If Hole's border is set, set its color
	 *
	 * @return bubble itself
	 */
	public HelpBubble setBorderHoleColor(int borderHoleColor) {
		this.borderHoleColor = borderHoleColor;
		return this;
	}

	//
	//	Method related to Bubble display
	//

	/**
	 * Set the Animation when bubble is displayed, default animation is a fade in animation
	 *
	 * @param showAnimation Showing animation
	 * @return bubble itself
	 */
	public HelpBubble setShowAnimation(Animation showAnimation) {
		this.showAnimation = showAnimation;
		return this;
	}

	/**
	 * Set the duration of showing animation
	 *
	 * @param animationDuration duration in millisecond, default is 250
	 * @return bubble itself
	 */
	public HelpBubble setAnimationDuration(int animationDuration) {
		this.animationDuration = animationDuration;
		return this;
	}

	/**
	 * Show the bubble according the configuration
	 *
	 * @return bubble itself
	 */
	public HelpBubble show() {
		createHoleArea();
		positionElements();
		this.startAnimation(showAnimation);
		isVisible = true;
		return this;
	}

	/**
	 * Hide the bubble
	 *
	 * @return bubble itself
	 */
	public HelpBubble hide() {
		this.setVisibility(GONE);
		isVisible = false;
		return this;
	}

	/**
	 *
	 * @return True if bubble is visible, false otherwise.
	 */
	public boolean isVisible() {
		return isVisible;
	}
}