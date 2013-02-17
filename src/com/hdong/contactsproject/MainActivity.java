package com.hdong.contactsproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hdong.contactsproject.R;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

/**
 * 主界面，联系人列表
 * 
 * @author hdong
 * 
 */
public class MainActivity extends Activity
{
	private Cursor result = null;
	private ListView contactsList = null;
	public static SimpleAdapter simpleAdapter = null;
	public List<Map<String, Object>> allContacts = null;
	private boolean flag = true; // 全局变量，才会有作用
	String contactsId = null; // 选择的位置
	public int delPostion = 0; // 需要删除的item标记

	public static MainActivity instance = null; // 定义一个静态的上下文变量，在onCreate时赋值this，用于ContactInfo类的关闭时调用

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		instance = this;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.setTitle("我的通讯录");
	}

	@Override
	protected void onResume()
	{
		this.contactsList = (ListView) this.findViewById(R.id.contactList);

		this.result = this.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, "sort_key asc");
		this.startManagingCursor(this.result); // 将结果集交给容器管理
		this.allContacts = new ArrayList<Map<String, Object>>();

		for (this.result.moveToFirst(); !this.result.isAfterLast(); this.result.moveToNext())
		{
			Map<String, Object> contact = new HashMap<String, Object>();
			contact.put("_id",
					this.result.getInt(this.result.getColumnIndex(ContactsContract.Contacts._ID)));
			contact.put("name", this.result.getString(this.result
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
			this.allContacts.add(contact);
		}

		this.simpleAdapter = new SimpleAdapter(this, this.allContacts, R.layout.contacts,
				new String[] { "name" }, new int[] { R.id.name });
		this.contactsList.setAdapter(simpleAdapter);
		this.contactsList.setOnItemClickListener(itemClickListener); // 添加联系人列表项的点击事件监听
		this.registerForContextMenu(this.contactsList); // 注册菜单

		// ****************************************************************
		Intent intent = this.getIntent();
		boolean isDelete = intent.getBooleanExtra("isDelete", false);
		System.out.println("isDelete = " + isDelete);
		if (isDelete)
		{
			this.allContacts.remove(0);
			this.simpleAdapter.notifyDataSetChanged(); // 进行刷新
		}
		// ****************************************************************
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// getMenuInflater().inflate(R.menu.activity_main, menu); 自带的settings项
		menu.add(0, Menu.FIRST + 1, 1, "关于Wo");
		menu.add(0, Menu.FIRST + 2, 1, "添加");
		menu.add(0, Menu.FIRST + 3, 1, "退出");
		return true; // 返回false，则Menu键失效
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case Menu.FIRST + 1: // 关于
			break;

		case Menu.FIRST + 2: // 添加
			Intent intent = new Intent(MainActivity.this, ContactAdd.class);
			this.startActivity(intent);
			break;

		case Menu.FIRST + 3: // 退出
			this.finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) // 创建菜单
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("联系人操作");
		menu.add(Menu.FIRST, Menu.FIRST + 1, 1, "查看详情");
		menu.add(Menu.FIRST, Menu.FIRST + 2, 1, "删除联系人");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		this.delPostion = info.position; // 取得操作为，并通过intent传递给ContactInfo中，用于删除操作
		contactsId = this.allContacts.get(this.delPostion).get("_id") // 通过get()方法从map中取值
				.toString();

		switch (item.getItemId())
		// 进行菜单操作
		{
		case Menu.FIRST + 1: // 查看
			Intent intent = new Intent("information");

			String phoneSelection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
			String[] phoneSelectionArgs = new String[] { contactsId };
			Cursor cursor = this.getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, phoneSelection,
					phoneSelectionArgs, null);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
			{
				intent.putExtra("number", cursor.getString(cursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
				intent.putExtra("name", cursor.getString(cursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
			}
			intent.putExtra("_id", contactsId);
			MainActivity.this.startActivity(intent);
			break;

		case Menu.FIRST + 2: // 删除
			new AlertDialog.Builder(this).setTitle("删除提示").setIcon(null).setMessage("您确定要删除吗？")
					.setPositiveButton("确定", new OnClickListener()
					{

						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							// 确定按钮的监听事件处理
							MainActivity.this.getContentResolver().delete(
									Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI,
											MainActivity.this.contactsId), null, null);
							MainActivity.this.allContacts.remove(MainActivity.this.delPostion); // 删除集合数据项
							MainActivity.this.simpleAdapter.notifyDataSetChanged(); // 通知改变
						}
					}).setNegativeButton("取消", new OnClickListener()
					{

						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							// 取消按钮的事件监听处理

						}
					}).show();
			break;

		default:
			break;
		}

		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) // 每次按键则初始化
	{

		if (keyCode == KeyEvent.KEYCODE_BACK && flag)
		{
			flag = false;
			Toast.makeText(this, "再点击一次退出程序！", Toast.LENGTH_LONG).show();
			return false;
		}
		else
		{
			return super.onKeyDown(keyCode, event);
		}
	}

	OnItemClickListener itemClickListener = new OnItemClickListener() // item的点击事件
	{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			String contactsId = MainActivity.this.allContacts.get(position).get("_id").toString();

			Intent intent = new Intent(MainActivity.this, ContactInfo.class);
			String phoneSelection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
			String[] phoneSelectionArgs = new String[] { contactsId };
			Cursor cursor = MainActivity.this.getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, phoneSelection,
					phoneSelectionArgs, null);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
			{
				intent.putExtra("number", cursor.getString(cursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
				intent.putExtra("name", cursor.getString(cursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
			}
			intent.putExtra("_id", contactsId);
			MainActivity.this.startActivity(intent);
		}
	};

}
