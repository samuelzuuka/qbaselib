package com.quseit.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.quseit.android.R;
import com.quseit.util.ImageDownLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AdSlidShowView extends FrameLayout {
	private String tag = "AdSlidShowView";
	private android.support.v4.view.ViewPager viewPageAd;
	private LinearLayout dotList;
	private Context context;

	private List<ImageView> adlistImage;
	private int currentItem = 0;
	public urlBackcall adBackCall;
	private ScheduledExecutorService scheduledExecutorService;

	public interface urlBackcall {
		void onUrlBackCall(int i);
	}

	public void setOnUrlBackCall(urlBackcall callBack) {
		adBackCall = callBack;
	}

	public AdSlidShowView(Context context) {
		super(context);
		this.context = context;
		initView(context, null);
	}

	public AdSlidShowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initView(context, attrs);
		initViewPager();
		startPlay();
	}

	/**
	 * 使用 List<ImageView> 设置轮播
	 * 
	 * @param adlistImage
	 */
	public void setAdImageList(List<ImageView> adlistImage) {
		dotList.removeAllViews();
		this.adlistImage = adlistImage;
		setDotLists(this.adlistImage);
	}

	/**
	 * 加载视图 默认有 5个 轮播
	 * 
	 * 1 设置setAdImageList 参数 List<ImageView> imgs 改变轮播
	 * 
	 * 2 设置getAdListImage 参数 List<String> Urls 改变轮播
	 * 
	 * @param context
	 * @param attrs
	 */
	private void initView(Context context, AttributeSet attrs) {

		LayoutInflater.from(context).inflate(R.layout.ad_viewpage_view, this);
		int[] resimg = new int[] {  };
		adlistImage = new ArrayList<ImageView>();

		for (int i = 0; i < resimg.length; i++) {
			ImageView imgView = new ImageView(context);
			imgView.setBackgroundResource(resimg[i]);
			adlistImage.add(imgView);
		}

	}

	private void initViewPager() {
		viewPageAd = (ViewPager) findViewById(R.id.ad_viewPager);
		viewPageAd.setFocusable(true);
		viewPageAd.setAdapter(new adviewPagerAdpter());
		viewPageAd.setOnPageChangeListener(new adviewpagerListener());
		dotList = (LinearLayout) findViewById(R.id.ll_dotparent);
	}

	private class adviewPagerAdpter extends PagerAdapter {
		@Override
		public int getCount() {
			// Log.i(tag, "adviewPagerAdpter" + adlistImage.size());
			return adlistImage.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {

		}

		@Override
		public Object instantiateItem(View container, final int position) {
			Log.d("AdSlidShowView", "instantiateItem:"+adlistImage.size()+"-"+position);
			if (adlistImage.size()!=0) {
				
				try{
				((ViewGroup) container).addView(adlistImage.get(position% adlistImage.size()),0);
				}catch(Exception e){
					
				}
				
				ImageView img = adlistImage.get(position % adlistImage.size());
				img.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						adBackCall.onUrlBackCall(position % adlistImage.size());
					}
				});
				return adlistImage.get(position % adlistImage.size());
			} else {
				return adlistImage.get(position % adlistImage.size());
			}

		}

	}

	private class adviewpagerListener implements OnPageChangeListener {
		boolean isAutoPlay = false;

		@Override
		public void onPageScrollStateChanged(int arg0) {
			//Log.d("AdSlidShowView", "onPageScrollStateChanged:"+arg0);
			switch (arg0) {
			case 1:
				isAutoPlay = false;
				stopPlay();
				break;
			case 2:
				isAutoPlay = true;
				break;
			case 0:
				if (viewPageAd.getCurrentItem() == viewPageAd.getAdapter()
						.getCount() - 1 && !isAutoPlay) {
					viewPageAd.setCurrentItem(0,false);
				} else if (viewPageAd.getCurrentItem() == 0 && !isAutoPlay) {
					viewPageAd.setCurrentItem(viewPageAd.getAdapter()
							.getCount() - 1,false);
				}

				break;

			}

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int arg0) {
			// Log.i(tag, "" + arg0);

			currentItem = arg0;
			for (int i = 0; i < dotList.getChildCount(); i++) {
				if (i == arg0) {
					((View) dotList.getChildAt(i))
							.setBackgroundResource(R.drawable.dian);
				} else {
					((View) dotList.getChildAt(i))
							.setBackgroundResource(R.drawable.dian_down);
				}
			}
		}
	}

	Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			viewPageAd.setCurrentItem(currentItem);
		};
	};

	private class SlidShowTask implements Runnable {
		public void run() {
			synchronized (viewPageAd) {
				currentItem = (currentItem + 1) % adlistImage.size();
				handler.obtainMessage().sendToTarget();
			}
		}

	}

	private void startPlay() {
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutorService.scheduleAtFixedRate(new SlidShowTask(), 1, 4,
				TimeUnit.SECONDS);
	}
	
	private void stopPlay(){
		scheduledExecutorService.shutdown();
	}

	public void setImagesFromUrl(List<String> urls) {
		Log.d("BS", "setImageFromUrl:" + urls.toString());
		dotList.removeAllViews();
		this.adlistImage = getAdListImage(urls);
		setDotLists(this.adlistImage);
		ImageDownLoader loader = new ImageDownLoader(this.getContext());
		
/*
		Random rd=new Random();
		int count=Math.abs(rd.nextInt()%2);
		if(count==0){
			List<String> res=new ArrayList<String>();
			for(int i=urls.size()-1;i>=0;i--){
				res.add(urls.get(i));
			}
			urls=res;
		}
*/
		
		for (int i = 0; i < urls.size(); i++) {
			String url = urls.get(i);
			final int index = i;
			Bitmap bitmap = loader.getBitmapCache(url);
			if (bitmap != null) {
				setImage(bitmap, i);
			} else {
				if (loader.getTaskCollection().containsKey(url)) {
					return;
				}

				loader.loadImage(url, this.getWidth(), this.getHeight(),
						new ImageDownLoader.AsyncImageLoaderListener() {

							@Override
							public void onImageLoader(Bitmap bitmap) {
								if (this != null && bitmap != null) {
									setImage(bitmap, index);
									// Log.e("錯誤的", index+"");
								}
							}

						});
			}
		}
	}

	/**
	 * 使用 List<String> urls 设置轮播
	 * 
	 * @param urls   链接集合
	 *           
	 * @return
	 */
	private List<ImageView> getAdListImage(List<String> urls) {
		List<ImageView> imgs = new ArrayList<ImageView>();
		for (int i = 0; i < urls.size(); i++) {
			ImageView view = new ImageView(context);
			view.setBackgroundResource(R.drawable.splash_port);
			imgs.add(view);
		}
		return imgs;
	}

	public void setDotLists(List<ImageView> adlistImage) {

		if (adlistImage.size() != 1) {
			for (int i = 0; i < adlistImage.size(); i++) {
				View v = new View(context);
				if (i == 0)
					v.setBackgroundResource(R.drawable.dian);
				else
					v.setBackgroundResource(R.drawable.dian_down);
				v.setLayoutParams(new LayoutParams(10, 10));
				dotList.addView(v);
			}
		}
		initViewPager();
	}

	public void setImage(Bitmap bitmap, int i) {
		ImageView view = adlistImage.get(i);
		Drawable drawable = new BitmapDrawable(bitmap);
		view.setBackgroundDrawable(drawable);
		requestLayout();
		invalidate();
	}

}
