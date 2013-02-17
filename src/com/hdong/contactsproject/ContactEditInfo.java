package com.hdong.contactsproject;

import java.util.ArrayList;

import com.hdong.contactsproject.R;
import com.hdong.service.IService;
import com.hdong.service.PhoneService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class ContactEditInfo extends Activity
{
	private EditText infoName = null; // 姓名
	private EditText infoNumber = null; // 电话号码
	private CheckBox checkBox = null; // 来电静音复选框
	private CheckBox checkBox2 = null; // 禁止来电复选框
	private Button btn_cancel = null; // 取消按钮
	private Button btn_ok = null; // 确定按钮
	private String id = null; // id
	private String name = null; // 姓名
	private String number = null; // 电话号码
	private IService service = null;
	private ServiceConnetionImpl serviceConnetionImpl = new ServiceConnetionImpl();

	public static final String ACTION_VOICE = "VOICE";

	private void init_1()
	{
		Intent intent = this.getIntent();
		this.id = intent.getStringExtra("id");

		System.out.println("this.id = " + this.id);// ------------------------

		this.name = intent.getStringExtra("name1");
		this.number = intent.getStringExtra("number1");
		this.infoName.append(name);
		this.infoNumber.setText(number);

	}

	private void init_2()
	{
		this.name = this.infoName.getText().toString();
		this.number = this.infoNumber.getText().toString();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.contact_edit_info);
		this.setTitle("编辑联系人");

		this.infoName = (EditText) this.findViewById(R.id.infoName);
		this.infoNumber = (EditText) this.findViewById(R.id.infoNumber);
		this.btn_cancel = (Button) this.findViewById(R.id.btn_cancel);
		this.btn_ok = (Button) this.findViewById(R.id.btn_ok);
		this.checkBox = (CheckBox) this.findViewById(R.id.checkBox1);
		this.checkBox2 = (CheckBox) this.findViewById(R.id.checkBox2);
		this.init_1(); // 数据初始化
		this.btn_cancel.setOnClickListener(clickListener); // 设置取消按钮的点击事件监听
		this.btn_ok.setOnClickListener(clickListener); // 设置确定按钮的点击事件监听
		this.checkBox.setOnCheckedChangeListener(changeListener); // 设置过滤复选框的状态改变事件监听
		this.checkBox2.setOnCheckedChangeListener(changeListener);

		// 加载时取得初始值
		SharedPreferences sharedPreferences = this.getSharedPreferences(
				this.number, MODE_WORLD_READABLE | MODE_WORLD_WRITEABLE);
		if (sharedPreferences != null)
		{
			checkBox.setChecked(sharedPreferences.getBoolean("isSilent", false));
			checkBox2.setChecked(sharedPreferences.getBoolean("isHoldDown",
					false));
		}

	}

	OnClickListener clickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			switch (v.getId())
			{
			case R.id.btn_cancel:
				ContactEditInfo.this.finish();
				break;

			case R.id.btn_ok:
				try
				{
					ContactEditInfo.this.init_2();
					ContactEditInfo.this.updateContact();
					System.out.println("更新方法执行完毕");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				// 只有点击确定按钮时，才记录checkbox的更改状态，点击取消按钮时不记录更改状态
				ContactEditInfo.this.writeToFile(ContactEditInfo.this.number,
						ContactEditInfo.this.checkBox.isChecked(),
						ContactEditInfo.this.checkBox2.isChecked());

				ContactEditInfo.this.finish();
				break;

			default:
				break;
			}

		}
	};

	OnCheckedChangeListener changeListener = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked)
		{
			Intent intent = new Intent(ACTION_VOICE);

			if (isChecked)
			{
				Intent intent1 = new Intent(ContactEditInfo.this,
						PhoneService.class);
				intent1.putExtra("phoneNumber", ContactEditInfo.this.number);
				ContactEditInfo.this.bindService(intent1, serviceConnetionImpl,
						Context.BIND_AUTO_CREATE);
				intent.putExtra("flag",
						ContactEditInfo.this.checkBox.isChecked());
				intent.putExtra("flag2",
						ContactEditInfo.this.checkBox2.isChecked());
				ContactEditInfo.this.sendBroadcast(intent);
			}
			else
			{
				intent.putExtra("flag", checkBox.isChecked());
				intent.putExtra("flag2", checkBox2.isChecked());
				ContactEditInfo.this.sendBroadcast(intent);

				if (ContactEditInfo.this.service != null)
				{
					Intent intent1 = new Intent(ContactEditInfo.this,
							PhoneService.class);
					ContactEditInfo.this.stopService(intent1);
				}
			}
		}
	};

	private void writeToFile(String phoneNumber, boolean a, boolean b)
	{
		SharedPreferences sharedPreferences = this.getSharedPreferences(
				phoneNumber, MODE_WORLD_READABLE | MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("isSilent", a);
		editor.putBoolean("isHoldDown", b);
		editor.commit();
	}

	@Override
	protected void onResume()
	{
		Intent intent = new Intent(ACTION_VOICE);
		intent.putExtra("flag", checkBox.isChecked());
		intent.putExtra("flag2", checkBox2.isChecked());
		sendBroadcast(intent);
		super.onResume();
	}

	private class ServiceConnetionImpl implements ServiceConnection
	{

		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			ContactEditInfo.this.service = (com.hdong.service.PhoneService.BinderImpl) service; // ------------
			try
			{
				Toast.makeText(ContactEditInfo.this,
						service.getInterfaceDescriptor(), 1000).show();
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name)
		{

		}

	}

	private void updateContact() throws Exception
	{

		System.out.println("执行了更新操作---------this.id------  " + this.id);

		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

		Uri uri = Uri.parse("content://com.android.contacts/data");
		ContentProviderOperation cpo1 = ContentProviderOperation
				.newUpdate(uri)
				.withSelection(
						Data.CONTACT_ID + " =? " + " and " + Data.MIMETYPE
								+ " =? ",
						new String[] { this.id,
								StructuredName.CONTENT_ITEM_TYPE })
				.withValue(StructuredName.DISPLAY_NAME, this.name).build();
		operations.add(cpo1);

		ContentProviderOperation cpo2 = ContentProviderOperation
				.newUpdate(uri)
				.withSelection(
						Data.CONTACT_ID + " = ? " + " and " + Data.MIMETYPE
								+ " =? ",
						new String[] { this.id, Phone.CONTENT_ITEM_TYPE })
				.withValue(Phone.NUMBER, this.number).build();
		operations.add(cpo2);

		this.getContentResolver().applyBatch(ContactsContract.AUTHORITY,
				operations);

		System.out.println("this.name = " + this.name);// ------------------
		System.out.println("this.number = " + this.number);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return false;
	}
}
