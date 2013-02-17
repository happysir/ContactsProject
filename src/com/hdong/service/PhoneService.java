package com.hdong.service;

import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;
import com.hdong.contactsproject.ContactEditInfo;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class PhoneService extends Service
{
	private TelephonyManager telephonyManager = null;
	private AudioManager audioManager = null; // 声音服务
	private String phoneNumber = null; // 要过滤的电话号码
	private IBinder myBinder = new BinderImpl();
	ITelephony iTelephony = null;

	MyReceiver receiver = null;

	// 是否静音
	boolean flag_Silent = false;
	// 是否直接挂断
	boolean flag_Over = false;
	public class BinderImpl extends Binder implements IService
	{
		@Override
		public String getInterfaceDescriptor()
		{
			return "过滤电话 “" + PhoneService.this.phoneNumber + "”设置成功!";
		}
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		// flag = true;
		System.out.println("绑定了" + flag_Silent);
		this.phoneNumber = intent.getStringExtra("phoneNumber"); // 取得电话号码
		this.audioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		this.telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		this.telephonyManager.listen(new PhoneStateListenerImpl(),
				PhoneStateListener.LISTEN_CALL_STATE);
		return this.myBinder;
	}

	private class PhoneStateListenerImpl extends PhoneStateListener
	{
		@Override
		public void onCallStateChanged(int state, String incomingNumber)
		{
			switch (state)
			{
			case TelephonyManager.CALL_STATE_IDLE: // 挂断电话
				PhoneService.this.audioManager
						.setRingerMode(AudioManager.RINGER_MODE_NORMAL); // 正常响铃模式
				break;

			case TelephonyManager.CALL_STATE_RINGING: // 拨入电话
				if ((incomingNumber.replaceAll(" ", ""))
						.equals(PhoneService.this.phoneNumber.replaceAll(" ",
								"")))
				{
					System.out.println("拨打电话····flag_Silent = " + flag_Silent);
					System.out.println("拨打电话····flag_Over = " + flag_Over);
					System.out.println("********************************");
					if (flag_Silent)
					{
						PhoneService.this.audioManager
								.setRingerMode(AudioManager.RINGER_MODE_SILENT); // 静音模式
					}
					else
					{
						PhoneService.this.audioManager
								.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
					}

					if (flag_Over)
					{
						phoner();
						if (iTelephony != null)
						{

						}
						try
						{
							iTelephony.endCall();
							System.out.println("拦截了一个电话，号码为：    "
									+ incomingNumber);
						}
						catch (RemoteException e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						iTelephony = null;
					}
					break;
				}

			case TelephonyManager.CALL_STATE_OFFHOOK: // 拨打电话
				break;

			default:
				break;
			}
		}
	}

	public class MyReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			System.out.println("接收器开始了");
			System.out.println("intent.getAction() = " + intent.getAction());

			if (intent.getAction().equals(ContactEditInfo.ACTION_VOICE))
			{
				flag_Silent = intent.getBooleanExtra("flag", true);
				flag_Over = intent.getBooleanExtra("flag2", true);
			}
			System.out.println("flag_Silent = " + flag_Silent);
			System.out.println("flag_Over = " + flag_Over);
			System.out.println("---------------------------------");
		}
	}

	public void phoner()
	{

		Class<TelephonyManager> c = TelephonyManager.class;
		Method getITelephonyMethod = null;
		try
		{
			getITelephonyMethod = c.getDeclaredMethod("getITelephony");
			getITelephonyMethod.setAccessible(true);
			iTelephony = (ITelephony) getITelephonyMethod
					.invoke(telephonyManager);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	};

	@Override
	public boolean onUnbind(Intent intent)
	{
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate()
	{
		// 注册广播接收者
		IntentFilter filter = new IntentFilter();
		filter.addAction(ContactEditInfo.ACTION_VOICE);
		receiver = new MyReceiver();
		registerReceiver(receiver, filter);
		super.onCreate();
	}

	@Override
	public void onDestroy()
	{
		unregisterReceiver(receiver);
		super.onDestroy();
	}
}
