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
	private AudioManager audioManager = null; // ��������
	private String phoneNumber = null; // Ҫ���˵ĵ绰����
	private IBinder myBinder = new BinderImpl();
	ITelephony iTelephony = null;

	MyReceiver receiver = null;

	// �Ƿ���
	boolean flag_Silent = false;
	// �Ƿ�ֱ�ӹҶ�
	boolean flag_Over = false;
	public class BinderImpl extends Binder implements IService
	{
		@Override
		public String getInterfaceDescriptor()
		{
			return "���˵绰 ��" + PhoneService.this.phoneNumber + "�����óɹ�!";
		}
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		// flag = true;
		System.out.println("����" + flag_Silent);
		this.phoneNumber = intent.getStringExtra("phoneNumber"); // ȡ�õ绰����
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
			case TelephonyManager.CALL_STATE_IDLE: // �Ҷϵ绰
				PhoneService.this.audioManager
						.setRingerMode(AudioManager.RINGER_MODE_NORMAL); // ��������ģʽ
				break;

			case TelephonyManager.CALL_STATE_RINGING: // ����绰
				if ((incomingNumber.replaceAll(" ", ""))
						.equals(PhoneService.this.phoneNumber.replaceAll(" ",
								"")))
				{
					System.out.println("����绰��������flag_Silent = " + flag_Silent);
					System.out.println("����绰��������flag_Over = " + flag_Over);
					System.out.println("********************************");
					if (flag_Silent)
					{
						PhoneService.this.audioManager
								.setRingerMode(AudioManager.RINGER_MODE_SILENT); // ����ģʽ
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
							System.out.println("������һ���绰������Ϊ��    "
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

			case TelephonyManager.CALL_STATE_OFFHOOK: // ����绰
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
			System.out.println("��������ʼ��");
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
		// ע��㲥������
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
