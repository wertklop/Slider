package org.slider;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SliderActivity extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Slider slider = new Slider(this);
        RelativeLayout relativeLayout = (RelativeLayout) RelativeLayout.inflate(this, R.layout.zero, null);
        final TextView text = (TextView) relativeLayout.findViewById(R.id.text);
        text.setOnClickListener( new OnClickListener() {
			public void onClick(View v) {
				Toast toast = Toast.makeText(getApplicationContext(), "Value: " + text.getText().toString(), Toast.LENGTH_SHORT);
				toast.show();
			}
		});	
        text.setClickable(false);
        slider.addView(relativeLayout);
        final int[] backgroundColors = { Color.RED, Color.BLUE};
		for (int i = 0; i < backgroundColors.length; i++) {
			final TextView textView = new TextView(getApplicationContext());
			textView.setText(Integer.toString(i + 1));
			textView.setId(i);
			textView.setTextSize(100);
			textView.setTextColor(Color.BLACK);
			textView.setGravity(Gravity.CENTER);
			textView.setBackgroundColor(backgroundColors[i]);
			textView.setOnClickListener( new OnClickListener() {
				public void onClick(View v) {
					Toast toast = Toast.makeText(getApplicationContext(), "Value: " + textView.getText().toString(), Toast.LENGTH_SHORT);
					toast.show();
				}
			});
			textView.setClickable(false);
			slider.addView(textView);
		}
        setContentView(slider);
    }
}