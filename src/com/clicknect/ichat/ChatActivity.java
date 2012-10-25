package com.clicknect.ichat;

import java.io.File;
import java.util.ArrayList;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.clicknect.android.ichat.R;
import com.clicknect.ichat.adapter.ChatAdapter;
import com.clicknect.ichat.entry.ChatEntry;
import com.clicknect.ichat.helper.BitmapUtility;

public class ChatActivity extends Activity {
	
	private static final int REQUEST_CODE_FOR_GALLERY = 1;
	private static final int REQUEST_CODE_FOR_FILEMANAGER = 2;
	private static String LOG_TAG = "- Chat Activity -";
	private Context context;
	
	private ListView chatListView;
	private Button attatchmentButton;
	private Button sendButton;
	private EditText chatMessageEditText;
	
	private ArrayList<ChatEntry> chatEntries;
	private ChatAdapter chatAdapter;
	
	private String fJID, fNickName;
	private String mJID, mNickName;
	private byte[] fAvatarByte, mAvatarByte;
	private Bitmap fAvatarBitmap, mAvatarBitmap;
	
	private XMPPConnection xmppConnection;
	private FileTransferManager receiveFileManager;
	private FileTransferManager sendFileManager;
	private FileTransferListener listener;
	private File externalStorageDirectory;
	private String fileSrc;	
	
	private Handler sHandler, rHandler;
	private Thread sThread;	
	private File rfile;
	
	private LinearLayout layoutReceivingFile;
	private LinearLayout layoutSendinfFile;
	private ProgressBar rProgressBar;
	private ProgressBar sProgressBar;
	
	private ChatManager chatManager;
	private Chat chat;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_chat);
		
		// Context
		context = this;
		
		// View matching
		chatListView = (ListView) findViewById(R.id.chat_message_listview);
		attatchmentButton = (Button) findViewById(R.id.chat_attatchment_button);
		sendButton = (Button) findViewById(R.id.chat_send_button);
		chatMessageEditText = (EditText) findViewById(R.id.chat_message_edit_text);
		layoutReceivingFile = (LinearLayout) findViewById(R.id.chat_file_transfer_receive_layout);
		layoutSendinfFile = (LinearLayout) findViewById(R.id.chat_file_transfer_send_layout);
		rProgressBar = (ProgressBar) findViewById(R.id.chat_receive_file_progress_bar);
		sProgressBar = (ProgressBar) findViewById(R.id.chat_send_file_progress_bar);
		
		// Event for attachment button
		attatchmentButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("File attachment");
				builder.setMessage("Choose your file type.");
				builder.setNegativeButton("Picture", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
						photoPickerIntent.setType("image/*");
						startActivityForResult(photoPickerIntent, REQUEST_CODE_FOR_GALLERY);
					}
				});
				builder.setPositiveButton("File", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(ChatActivity.this, FilePickerActivity.class);
						
						ArrayList<String> extensions = new ArrayList<String>();
						extensions.add(".png");
						extensions.add(".jpg");
						extensions.add(".gif");
						extensions.add(".tif");
						extensions.add(".bmp");
						
						extensions.add(".doc");
						extensions.add(".docx");
						extensions.add(".ppt");
						extensions.add(".pptx");
						extensions.add(".xls");
						extensions.add(".xlsx");
						extensions.add(".pdf");						
						
						extensions.add(".zip");
						extensions.add(".rar");						
						intent.putExtra(FilePickerActivity.EXTRA_ACCEPTED_FILE_EXTENSIONS, extensions);
						
						startActivityForResult(intent, REQUEST_CODE_FOR_FILEMANAGER);						
					}
				});
				builder.setCancelable(true);
				
				AlertDialog alertDialog = builder.create();
				alertDialog.show();
			}
		});
		
		// Event for send message button
		sendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				String message = chatMessageEditText.getText().toString();
				if ( message.trim().length() == 0 ) {
					Toast.makeText(context, "Please fill up some text.", Toast.LENGTH_SHORT).show();
				} else {					
					try {
						chat.sendMessage(message);
						chatMessageEditText.setText(null);
						
						ChatEntry chatEntry = new ChatEntry();
						chatEntry.setAttachedFile(false);
						chatEntry.setFilePath(null);
						chatEntry.setSenderJID(mJID);
						chatEntry.setReceiverJID(fJID);
						chatEntry.setSenderAvatarBitmap(mAvatarBitmap);
						chatEntry.setWhen(System.currentTimeMillis());
						chatEntry.setMessage(message);
						
						chatEntries.add(chatEntry);
						chatAdapter.setData(chatEntries);
						chatAdapter.notifyDataSetChanged();
						chatListView.setSelection(chatEntries.size()-1);
					} catch (XMPPException e) {
						Log.e(LOG_TAG, "Unable to send message: "+e.getMessage());
					}
				}
			}
		});		
		
		// Event for chat list view
		chatListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long which) {
				if ( chatEntries.get(position).isAttachedFile() ) {
					File file = new File(chatEntries.get(position).getFilePath());
					
					if ( isImageFile(file) ) {
						Intent intent = new Intent();
						intent.setAction(android.content.Intent.ACTION_VIEW);    
						intent.setDataAndType(Uri.fromFile(file), "image/*");  
						startActivity(intent); 		
					} else {
						Intent intent = new Intent(Intent.ACTION_VIEW);
					    intent.setData(Uri.fromFile(file));
					    startActivity(intent);						
					}
				}
			}
		});
		
		// Extras
		Bundle bundle = getIntent().getExtras();
		if ( bundle == null ) {
			ChatActivity.this.finish();
		} else {
			// Get active connection
			xmppConnection = MainActivity.getXMPPConnection();
			if ( xmppConnection == null ) {
				ChatActivity.this.finish();
			} else {
				// For BEEM
				FileTransferNegotiator.IBB_ONLY = true;
				// -- END OF FOR BEEM
				
				// Enable Server Discovery Manager
				ServiceDiscoveryManager sdm = new ServiceDiscoveryManager(xmppConnection);
				// sdm.addFeature("http://jabber.org/protocol/ibb");
				
				// FOR BEEM
				sdm.addFeature("http://jabber.org/protocol/disco#info");
			    sdm.addFeature("http://jabber.org/protocol/disco#item");
			    sdm.addFeature("jabber:iq:privacy");				
				// -- END OF FOR BEEM 
				
				// Receive data from extras
				fJID = bundle.getString("fJID");
				fJID += "/"+ApplicationSetting.SERVER_RESOURCE;
				
				Toast.makeText(context, fJID, Toast.LENGTH_SHORT).show();
				
				fNickName = bundle.getString("fNickName");
				fAvatarByte = bundle.getByteArray("fAvatarByte");
				fAvatarBitmap = BitmapUtility.convertByteArrayToBitmap(fAvatarByte);
				
				mJID = bundle.getString("mJID");
				mNickName = bundle.getString("mNickName");
				mAvatarByte = bundle.getByteArray("mAvatarByte");
				mAvatarBitmap = BitmapUtility.convertByteArrayToBitmap(mAvatarByte);
				
				// Initialize
				chatMessageEditText.setSingleLine(true);
				chatEntries = new ArrayList<ChatEntry>();
				chatAdapter = new ChatAdapter(context, chatEntries, mJID);
				chatListView.setAdapter(chatAdapter);
				chatListView.setDivider(null);
				fileSrc = null;
				rProgressBar.setMax(100);
				sProgressBar.setMax(100);
				sProgressBar.setClickable(false);
				rProgressBar.setClickable(false);
				layoutReceivingFile.setVisibility(View.GONE);
				layoutSendinfFile.setVisibility(View.GONE);				
				
				// Set title for this activity
				setTitle("Chat with "+fNickName);
				Log.i(LOG_TAG, mNickName+" chat with "+fNickName);
				
				// Create Chat Manager
				chatManager = xmppConnection.getChatManager();
				
				// Add Chat Listener
				chatManager.addChatListener(new ChatManagerListener() {
					public void chatCreated(Chat chat, boolean isCreated) {
						Log.i(LOG_TAG, "Chat was created.");
					}
				});
				
				// Create chat channel with friend
				final Handler chatHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						switch ( msg.what ) {							
							case 1:								
								org.jivesoftware.smack.packet.Message chatMessage;
								chatMessage = (org.jivesoftware.smack.packet.Message) msg.obj;
								
								ChatEntry chatEntry = new ChatEntry();
								chatEntry.setAttachedFile(false);
								chatEntry.setFilePath(null);
								chatEntry.setSenderJID(fJID);
								chatEntry.setReceiverJID(mJID);
								chatEntry.setSenderAvatarBitmap(fAvatarBitmap);
								chatEntry.setWhen(System.currentTimeMillis());
								chatEntry.setMessage(chatMessage.getBody());
								
								chatEntries.add(chatEntry);
								chatAdapter.setData(chatEntries);
								chatAdapter.notifyDataSetChanged();
								chatListView.setSelection(chatEntries.size()-1);
								break;
						}
					}
				};
				chat = chatManager.createChat(fJID, new MessageListener() {
					public void processMessage(Chat chat, final org.jivesoftware.smack.packet.Message chatMessage) {
						if ( chatMessage.getBody() != null ) {
							new Thread() {
								public void run() {
									Message msg = new Message();
									msg.what = 1;
									msg.obj = chatMessage;
									chatHandler.sendMessage(msg);
								};
							}.start();
							Log.i(LOG_TAG, "Received message from: "+chatMessage.getFrom()+": "+chatMessage.getBody());
						}
					}
				});				
				
				// Setup file transfer manager
				receiveFileManager = new FileTransferManager(xmppConnection);
				sendFileManager = new FileTransferManager(xmppConnection);
				
				// Setup default saving directory ('mychat' on sd card)
				externalStorageDirectory = new File(Environment.getExternalStorageDirectory(), "mychat");
				
				// Setup handler for receive file listener
				rHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						switch ( msg.what ) {
							case 0:
								// Finish with error
								layoutReceivingFile.setVisibility(View.GONE);
								Toast.makeText(context, "Unable to receive file.", Toast.LENGTH_LONG).show();
								break;
								
							case 1:
								// Finish with complete
								layoutReceivingFile.setVisibility(View.GONE);
								Toast.makeText(context, "You have received new file.", Toast.LENGTH_LONG).show();
								
								ChatEntry chatEntry = new ChatEntry();
								chatEntry.setAttachedFile(true);
								chatEntry.setFilePath(rfile.getAbsolutePath());
								chatEntry.setSenderJID(fJID);
								chatEntry.setReceiverJID(mJID);
								chatEntry.setSenderAvatarBitmap(fAvatarBitmap);
								chatEntry.setWhen(System.currentTimeMillis());
								chatEntry.setMessage("You just received an attachment.");
								
								chatEntries.add(chatEntry);
								chatAdapter.setData(chatEntries);
								chatAdapter.notifyDataSetChanged();
								chatListView.setSelection(chatEntries.size()-1);
								
								break;
								
							case 2:
								// Streaming
								layoutReceivingFile.setVisibility(View.VISIBLE);
								break;
						}
					}
				};
				
				// Setup receive file listener
				listener = new FileTransferListener() {
					public void fileTransferRequest(final FileTransferRequest request) {
						Log.i(LOG_TAG, "Receive file coming");
						new Thread() {
							public void run() {
								IncomingFileTransfer transfer = request.accept();
								rfile = new File(externalStorageDirectory, transfer.getFileName());
								try {
									transfer.recieveFile(rfile);
									Log.i(LOG_TAG, "Start receive file.");
									while ( !transfer.isDone() ) {
										try {
											sleep(1000);
											
											int percent = (int)(transfer.getProgress()*100);
											if ( percent == 100 ) {
												rProgressBar.setProgress(0);
											} else {
												rProgressBar.setProgress(percent);
											}
											
											Log.i(LOG_TAG, "Receiving file: "+percent+" %");
											rHandler.sendEmptyMessage(2);
										} catch ( InterruptedException e ) {
											e.printStackTrace();
											Log.e(LOG_TAG, "Receiving thread was interrupoted: "+e.getMessage());
										}
									}
									
									if ( transfer.getStatus().equals(Status.complete) ) {
										rHandler.sendEmptyMessage(1);
										Log.i(LOG_TAG, "Receive file Completed.");
									} else if ( transfer.getStatus().equals(Status.cancelled) ) {
										rHandler.sendEmptyMessage(0);
										Log.e(LOG_TAG, "Receive file Cancelled.");
									} else if ( transfer.getStatus().equals(Status.error) ) {
										rHandler.sendEmptyMessage(0);
										Log.e(LOG_TAG, "Receive file Error.");
									} else if ( transfer.getStatus().equals(Status.refused) ) {
										rHandler.sendEmptyMessage(0);
										Log.e(LOG_TAG, "Receive file Refused.");
									}
								} catch ( Exception e ) {
									Log.e(LOG_TAG, "Unable to receive file: "+e.getMessage());
								}
							};
						}.start();
					}
				};
				
				// Add receive file listener
				receiveFileManager.addFileTransferListener(listener);
			}
		}
	}
	
	// Send file
	public void sendFile(final File file) {				
		sHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch ( msg.what ) {
					case 0:
						// Finish with error
						layoutSendinfFile.setVisibility(View.GONE);
						Toast.makeText(context, "Unable to send file.", Toast.LENGTH_LONG).show();
						break;
						
					case 1:
						// Finish with complete
						layoutSendinfFile.setVisibility(View.GONE);
						Toast.makeText(context, "Your file has been sent.", Toast.LENGTH_LONG).show();
						
						ChatEntry chatEntry = new ChatEntry();
						chatEntry.setAttachedFile(true);
						chatEntry.setFilePath(fileSrc);
						chatEntry.setSenderJID(mJID);
						chatEntry.setReceiverJID(fJID);
						chatEntry.setSenderAvatarBitmap(mAvatarBitmap);
						chatEntry.setWhen(System.currentTimeMillis());
						chatEntry.setMessage("Your attachment has been sent.");
						
						chatEntries.add(chatEntry);
						chatAdapter.setData(chatEntries);
						chatAdapter.notifyDataSetChanged();
						chatListView.setSelection(chatEntries.size()-1);
						break;
						
					case 2:
						// Streaming
						layoutSendinfFile.setVisibility(View.VISIBLE);
						break;
				}
			}
		};
		
		sThread = new Thread() {
			@Override
			public void run() {
				Log.i(LOG_TAG, "Send file coming");
				OutgoingFileTransfer transfer = sendFileManager.createOutgoingFileTransfer(fJID);
				try {
					transfer.sendFile(file, "iChat-attachment-file");
					Log.i(LOG_TAG, "Starting send file");
					
					while ( !transfer.isDone() ) {
						try {
							sleep(1000);
							
							int percent = (int)(transfer.getProgress()*100);
							if ( percent == 100 ) {
								sProgressBar.setProgress(0);
							} else {
								sProgressBar.setProgress(percent);
							}							
							
							Log.i(LOG_TAG, "Sending file: "+percent+" %");
							sHandler.sendEmptyMessage(2);
						} catch (InterruptedException e) {
							e.printStackTrace();
							Log.e(LOG_TAG, "Send file thread was interrupted: "+e.getMessage());
						}
					}
					
					if ( transfer.getStatus().equals(Status.complete) ) {
						sHandler.sendEmptyMessage(1);
						Log.i(LOG_TAG, "Send file is completed");
					} else {
						sHandler.sendEmptyMessage(0);
						Log.e(LOG_TAG, "Send file is failed");
					}
				} catch (XMPPException e) {
					Log.e(LOG_TAG, "Unable to send file: "+e.getMessage());
				}
			}
		};
		sThread.start();
	}
	
	@Override
	  protected void onActivityResult(int requestCode, int resultcode, Intent intent) {
		  super.onActivityResult(requestCode, resultcode, intent);
		  if (requestCode == REQUEST_CODE_FOR_GALLERY) {
			  if (intent != null) {				  
				  Cursor cursor = getContentResolver().query(intent.getData(), null, null, null, null);
				  cursor.moveToFirst();
				  
				  int idx = cursor.getColumnIndex(ImageColumns.DATA);
				  fileSrc = cursor.getString(idx);
				  
				  File file = new File(fileSrc);
				  if ( file.exists() ) {
					  sendFile(file);
				  } else {
					  Toast.makeText(context, "Image was not found, please try again.", Toast.LENGTH_LONG).show();
				  }
			  } else {
				  Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show();
			  }
		  } else if (requestCode == REQUEST_CODE_FOR_FILEMANAGER) {
			  if ( resultcode == RESULT_OK ) {
				  if( intent.hasExtra(FilePickerActivity.EXTRA_FILE_PATH) ) {
						File file = new File(intent.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH));
						
						// Set the file path text view
						if ( file.exists() && isAllowedFileExtension(file) ) {
							sendFile(file);
						} else {
							Toast.makeText(context, "This file is not allowed, please choose another file.", Toast.LENGTH_LONG).show();
						}
					}				  
			  } else {
				  Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show();
			  }
		  }
	  }
	
	public boolean isAllowedFileExtension(File file) {
		boolean result = false;
		
		String filename = file.getName();
		int length = filename.length();
		
		int correctPosition = 0;
		String ext = "";
		for ( int i = length-1; i > 0; i-- ) {
			if ( filename.charAt(i) == '.' ) {
				correctPosition = i;
				break;
			}
		}
		
		if ( correctPosition == 0 ) {
			Toast.makeText(context, "This file is invalid, please choose another file.", Toast.LENGTH_LONG).show();
		} else {
			for ( int i = correctPosition+1; i < length; i++ ) {
				ext += filename.charAt(i);
			}
			
			if ( ext.trim().length() == 0 ) {
				Toast.makeText(context, "This file is invalid, please choose another file.", Toast.LENGTH_LONG).show();
			} else {
				boolean isAllowedExtension = false;
				String[] allowedExtension = new String[] { "png", "jpg", "gif", "tif", "bmp", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "pdf", "zip", "rar" };
				for ( int i = 0; i < allowedExtension.length; i++ ) {
					if ( ext.equalsIgnoreCase(allowedExtension[i]) ) {
						isAllowedExtension = true;
					}
				}
				
				if ( isAllowedExtension ) {
					result = true;
				}
			}
		}
		
		return result;
	}
	
	public boolean isImageFile(File file) {
		boolean result = false;
		
		String filename = file.getName();
		int length = filename.length();
		
		int correctPosition = 0;
		String ext = "";
		for ( int i = length-1; i > 0; i-- ) {
			if ( filename.charAt(i) == '.' ) {
				correctPosition = i;
				break;
			}
		}
		
		if ( correctPosition == 0 ) {
			Toast.makeText(context, "This file is invalid, please choose another file.", Toast.LENGTH_LONG).show();
		} else {
			for ( int i = correctPosition+1; i < length; i++ ) {
				ext += filename.charAt(i);
			}
			
			if ( ext.trim().length() == 0 ) {
				Toast.makeText(context, "This file is invalid, please choose another file.", Toast.LENGTH_LONG).show();
			} else {
				boolean isAllowedExtension = false;
				String[] allowedExtension = new String[] { "png", "jpg", "gif", "tif", "bmp" };
				for ( int i = 0; i < allowedExtension.length; i++ ) {
					if ( ext.equalsIgnoreCase(allowedExtension[i]) ) {
						isAllowedExtension = true;
					}
				}
				
				if ( isAllowedExtension ) {
					result = true;
				}
			}
		}
		
		return result;
	}

}
