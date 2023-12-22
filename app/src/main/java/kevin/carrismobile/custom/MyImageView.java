package kevin.carrismobile.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class MyImageView extends ImageView {

    private Paint paint;
    private String text;
    public MyImageView(Context context) {
        super(context);
    }

    public MyImageView(Context context, Paint paint, String text) {
        super(context);
        this.paint = paint;
        this.text = text;
    }

    public MyImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        newOnDraw(canvas, 0, 25, text, paint);
        super.onDraw(canvas);
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public void setText(String text) {
        this.text = text;
    }

    protected void newOnDraw(Canvas canvas, int xpos, int ypos, String text, Paint paint) {
        canvas.drawText(text, xpos, ypos, paint);
    }
}
