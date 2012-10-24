package com.clicknect.ichat;

import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.provider.BytestreamsProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.IBBProviders;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.clicknect.android.ichat.R;
import com.clicknect.ichat.adapter.ContactAdapter;
import com.clicknect.ichat.entry.ContactEntry;
import com.clicknect.ichat.helper.BitmapUtility;
import com.clicknect.ichat.helper.UserPreferences;

public class MainActivity extends Activity {
	
	public static String LOG_TAG = "- Main Activity -";
	
	public static final int LOGIN_COMPLETE = 1;
	public static final int LOGIN_FAIL = 2;
	public static final int CONNECTION_COMPLETE = 3;	
	public static final int CONNECTION_FAIL = 4;	
	
	private Context context;
	private String username, password;
	private String userJID;
	private String userNickName;
	private byte[] userAvatarByte;
	
	private UserPreferences userPreferences;
	
	private LinearLayout mainLayout;
	private ImageView userAvatarImage;
	private TextView userDisplayNameText;
	private TextView userFriendsListText;
	private ListView userContactListView;	
	
	private VCard userVCard;
	private Roster userRoster;
	private ArrayList<ContactEntry> userContactEntries;
	private ContactAdapter userContactAdapter;	
	
	private static XMPPConnection xmppConnection;
	private Thread loginThread;
	private Handler loginHandler;
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);
		
		// Context
		context = this;
		
		// User Preferences
		userPreferences = new UserPreferences(context);
		
		// Progress Dialog
		progressDialog = new ProgressDialog(context);
		progressDialog.setMessage("Connecting");
		
		// Get Extras from Splash Activity
		Bundle bundle = getIntent().getExtras();
		if ( bundle == null ) {
			startActivity(new Intent(MainActivity.this, LoginActivity.class));
			MainActivity.this.finish();
		} else {
			// Setup username and password
			username = bundle.getString("username");
			password = bundle.getString("password");
			
			// View matching
			mainLayout = (LinearLayout) findViewById(R.id.main_layout);
			userAvatarImage = (ImageView) findViewById(R.id.main_user_avatar);
			userDisplayNameText = (TextView) findViewById(R.id.main_user_display_name);
			userFriendsListText = (TextView) findViewById(R.id.main_divider_friends);
			userContactListView = (ListView) findViewById(R.id.main_contact_listview);
			
			// Initialize
			mainLayout.setVisibility(View.GONE);
			userContactEntries = new ArrayList<ContactEntry>();
			userContactAdapter = new ContactAdapter(context, userContactEntries);
			userContactListView.setAdapter(userContactAdapter);
			
			// Event
			userContactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View view, int position, long which) {
					Intent intent = new Intent(MainActivity.this, ChatActivity.class);
					
					intent.putExtra("fJID", userContactEntries.get(position).getJid());
					intent.putExtra("fNickName", userContactEntries.get(position).getNickname());					
					intent.putExtra("fAvatarByte", userContactEntries.get(position).getAvatarByte());
					
					intent.putExtra("mJID", userJID);
					intent.putExtra("mNickName", userNickName);
					intent.putExtra("mAvatarByte", userAvatarByte);
					
					startActivity(intent);
				}
			});
			
			// Start connect to server and log in
			doConnectAndLogin();
		}
	}
	
	public void doConnectAndLogin() {
		progressDialog.show();
		loginHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch ( msg.what ) {
					// Connection fail
					case CONNECTION_FAIL:
						progressDialog.dismiss();
						Toast.makeText(context, "Working internet connection is required.", Toast.LENGTH_LONG).show();
						
						userPreferences.clearCurrentPassword();
						userPreferences.clearCurrentUsername();
						
						startActivity(new Intent(MainActivity.this, LoginActivity.class));
						MainActivity.this.finish();
						
						break;
					
					// Connection complete
					case CONNECTION_COMPLETE:
						progressDialog.setMessage("Logging In");
						break;
						
					// Log in fail
					case LOGIN_FAIL:
						progressDialog.dismiss();
						Toast.makeText(context, "Unable to log in, please try again.", Toast.LENGTH_LONG).show();
						
						userPreferences.clearCurrentPassword();
						userPreferences.clearCurrentUsername();
						
						startActivity(new Intent(MainActivity.this, LoginActivity.class));
						MainActivity.this.finish();
						
						break;
					
					// Log in complete
					case LOGIN_COMPLETE:
						progressDialog.setMessage("Success");
						progressDialog.dismiss();
						Toast.makeText(context, userJID, Toast.LENGTH_SHORT).show();
						
						userPreferences.saveCurrentUsername(username);
						userPreferences.saveCurrentPassword(password);
						
						// Display main layout
						mainLayout.setVisibility(View.VISIBLE);
						
						// Get user information
						// - user profile
						userVCard = new VCard();
						try {
							userVCard.load(xmppConnection);
							
							userNickName = userVCard.getNickName();
							userAvatarByte = userVCard.getAvatar();
							
							if ( userNickName == null ) {
								userDisplayNameText.setText(userJID);
							} else {
								userDisplayNameText.setText(userNickName);
							}
							
							if ( userAvatarByte != null ) {
								userAvatarImage.setImageBitmap(BitmapUtility.convertByteArrayToBitmap(userAvatarByte));
							} else {
								Toast.makeText(context, "You don't have avatar.", Toast.LENGTH_SHORT).show();
							}
						} catch (XMPPException e) {
							Log.e(LOG_TAG, "Unable to load user VCard: "+e.getMessage());
						}
						
						// - user's friends (Roster)
						userRoster = xmppConnection.getRoster();
						Collection<RosterEntry> rosterEntries = userRoster.getEntries();
						if ( rosterEntries.size() == 0 ) {
							Toast.makeText(context, "You don't have any friend.", Toast.LENGTH_SHORT).show();
						} else {
							userFriendsListText.append(" ("+rosterEntries.size()+")");
							
							for ( RosterEntry rosterEntry : rosterEntries ) {
								final VCard fVCard = new VCard();
								try {
									Log.i(LOG_TAG, "fJID: "+rosterEntry.getUser());
									fVCard.load(xmppConnection, rosterEntry.getUser());
									ContactEntry contactEntry = new ContactEntry();
									
									contactEntry.setJid(rosterEntry.getUser());
									
									if ( fVCard.getNickName() == null ) {
										contactEntry.setNickname(rosterEntry.getUser());
									} else {
										contactEntry.setNickname(fVCard.getNickName());
									}
									
									if ( fVCard.getAvatar() == null ) {
										contactEntry.setAvatarBitmap(null);
										contactEntry.setAvatarByte(null);
									} else {
										contactEntry.setAvatarByte(fVCard.getAvatar());
										contactEntry.setAvatarBitmap(BitmapUtility.convertByteArrayToBitmap(fVCard.getAvatar()));
									}
									
									userContactEntries.add(contactEntry);
									userContactAdapter.setData(userContactEntries);
									userContactAdapter.notifyDataSetChanged();
								} catch (XMPPException e) {
									Log.e(LOG_TAG, "Unable to load friend VCard: "+e.getMessage());
								}
							}
						}
						
						break;
				}
			}
		};
		
		loginThread = new Thread() {
			@Override
			public void run() {
				boolean isConnected;
				// Trying connect to server
				try {					
					// Setup required features
					configure(ProviderManager.getInstance());
					
					// Setup connection configuration
					ConnectionConfiguration config = new ConnectionConfiguration(ApplicationSetting.SERVER_IP,  ApplicationSetting.SERVER_PORT);
					config.setSASLAuthenticationEnabled(true);
					SASLAuthentication.supportSASLMechanism("PLAIN", 0);
					
					// Setup XMPP connection from configuration
					xmppConnection = new XMPPConnection(config);
					
					// Connect to server
					xmppConnection.connect();
					isConnected = true;
					Log.i(LOG_TAG, "Connected to connect server.");
					loginHandler.sendEmptyMessage(CONNECTION_COMPLETE);
				} catch ( XMPPException e ) {
					isConnected = false;
					Log.e(LOG_TAG, "Unable to connect server: "+e.getMessage());
					loginHandler.sendEmptyMessage(CONNECTION_FAIL);
				}

				// Trying log in to server
				if ( isConnected ) {
					try {
						xmppConnection.login(username, password, ApplicationSetting.SERVER_RESOURCE);
						userJID = xmppConnection.getUser();
						Log.i(LOG_TAG, "Logged in to connect server.");
						loginHandler.sendEmptyMessage(LOGIN_COMPLETE);
					} catch (XMPPException e) {
						Log.e(LOG_TAG, "Unable to log in: "+e.getMessage());						
						loginHandler.sendEmptyMessage(LOGIN_FAIL);
					}
				} else {
					loginHandler.sendEmptyMessage(CONNECTION_FAIL);
				}
			}
		};
		loginThread.start();		
	}
	
	public void configure(ProviderManager pm) {
		// Enable VCard feature
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
		
		// User Search
		pm.addIQProvider("query","jabber:iq:search", new UserSearch.Provider());
		
		//  Private Data Storage
        pm.addIQProvider("query","jabber:iq:private", new PrivateDataManager.PrivateDataIQProvider());
 
        //  Time
        try {
            pm.addIQProvider("query","jabber:iq:time", Class.forName("org.jivesoftware.smackx.packet.Time"));
        } catch (ClassNotFoundException e) {
            Log.e(LOG_TAG, "Can't load class for org.jivesoftware.smackx.packet.Time");
        }
 
        //  XHTML
        pm.addExtensionProvider("html","http://jabber.org/protocol/xhtml-im", new XHTMLExtensionProvider());

        //  Roster Exchange
        pm.addExtensionProvider("x","jabber:x:roster", new RosterExchangeProvider());
        
        //  Message Events
        pm.addExtensionProvider("x","jabber:x:event", new MessageEventProvider());
        
        //  Chat State
        pm.addExtensionProvider("active","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("composing","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("paused","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("inactive","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("gone","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        
        //  File Transfer - with ASMACK-ANDROID-7
        	pm.addIQProvider("si","http://jabber.org/protocol/si", new StreamInitiationProvider());
			pm.addIQProvider("query","http://jabber.org/protocol/bytestreams", new BytestreamsProvider());
        
        
        //  File Transfer - WITH ASMACK-ANDDROID-7-BEEM
        	pm.addIQProvider("si", "http://jabber.org/protocol/si", new StreamInitiationProvider());
            pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams", new BytestreamsProvider());
            pm.addIQProvider("open","http://jabber.org/protocol/ibb", new IBBProviders.Open());
            pm.addIQProvider("close","http://jabber.org/protocol/ibb", new IBBProviders.Close());
            pm.addExtensionProvider("data","http://jabber.org/protocol/ibb", new IBBProviders.Data());        
        
        
        
        
        
        //  Group Chat Invitations
        pm.addExtensionProvider("x","jabber:x:conference", new GroupChatInvitation.Provider());
        
        //  Service Discovery # Items    
        pm.addIQProvider("query","http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());
        
        //  Service Discovery # Info
        pm.addIQProvider("query","http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());
        
        //  Data Forms
        pm.addExtensionProvider("x","jabber:x:data", new DataFormProvider());
        
        //  MUC User
        pm.addExtensionProvider("x","http://jabber.org/protocol/muc#user", new MUCUserProvider());
        
        //  MUC Admin    
        pm.addIQProvider("query","http://jabber.org/protocol/muc#admin", new MUCAdminProvider());
        
        //  MUC Owner    
        pm.addIQProvider("query","http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());
        
        //  Delayed Delivery
        pm.addExtensionProvider("x","jabber:x:delay", new DelayInformationProvider());
        
        //  Version
        try {
            pm.addIQProvider("query","jabber:iq:version", Class.forName("org.jivesoftware.smackx.packet.Version"));
        } catch (ClassNotFoundException e) {
            Log.w(LOG_TAG, "Can't load class for org.jivesoftware.smackx.packet.Version");
        }
        
        //  Offline Message Requests
        pm.addIQProvider("offline","http://jabber.org/protocol/offline", new OfflineMessageRequest.Provider());
        
        //  Offline Message Indicator
        pm.addExtensionProvider("offline","http://jabber.org/protocol/offline", new OfflineMessageInfo.Provider());
        
        //  Last Activity
        pm.addIQProvider("query","jabber:iq:last", new LastActivity.Provider());
        
        //  SharedGroupsInfo
        pm.addIQProvider("sharedgroup","http://www.jivesoftware.org/protocol/sharedgroup", new SharedGroupsInfo.Provider());
        
        //  JEP-33: Extended Stanza Addressing
        pm.addExtensionProvider("addresses","http://jabber.org/protocol/address", new MultipleAddressesProvider());		
	}
	
	public static XMPPConnection getXMPPConnection() {
		return xmppConnection;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch ( item.getItemId() ) {
			case R.id.main_menu_add_friend:
				Toast.makeText(context, "Add friend is unavailable", Toast.LENGTH_SHORT).show();
				break;
				
			case R.id.main_menu_settings:
				Toast.makeText(context, "Settings is unavailable", Toast.LENGTH_SHORT).show();
				break;
				
			case R.id.main_menu_log_out:
				userPreferences.clearCurrentUsername();				
				userPreferences.clearCurrentPassword();
				onBackPressed();
				break;				
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void doDisconnect() {
		if ( xmppConnection != null ) {
			xmppConnection.disconnect();
			Log.i(LOG_TAG, "Disconnected from server.");
			Toast.makeText(context, "You have disconnected from server.", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onBackPressed() {
		doDisconnect();
		super.onBackPressed();
	}

}
