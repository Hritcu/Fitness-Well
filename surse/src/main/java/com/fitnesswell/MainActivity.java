package com.fitnesswell;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.fitnesswell.DAO.DAOMachine;
import com.fitnesswell.DAO.DAOProfile;
import com.fitnesswell.DAO.DatabaseHelper;
import com.fitnesswell.DAO.Machine;
import com.fitnesswell.DAO.Profile;
import com.fitnesswell.DAO.cardio.DAOOldCardio;
import com.fitnesswell.DAO.cardio.OldCardio;
import com.fitnesswell.DAO.program.DAOProgram;
import com.fitnesswell.DAO.record.DAOCardio;
import com.fitnesswell.DAO.record.DAOFonte;
import com.fitnesswell.DAO.record.DAORecord;
import com.fitnesswell.DAO.record.DAOStatic;
import com.fitnesswell.DAO.record.Record;
import com.fitnesswell.bodymeasures.BodyPartListFragment;
import com.fitnesswell.enums.DistanceUnit;
import com.fitnesswell.enums.ExerciseType;
import com.fitnesswell.enums.WeightUnit;
import com.fitnesswell.fonte.FontesPagerFragment;
import com.fitnesswell.intro.MainIntroActivity;
import com.fitnesswell.machines.MachineFragment;
import com.fitnesswell.programs.ProgramListFragment;
import com.fitnesswell.utils.DateConverter;
import com.fitnesswell.utils.ImageUtil;
import com.fitnesswell.utils.MusicController;
import com.fitnesswell.utils.UnitConverter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.onurkaganaldemir.ktoastlib.KToast;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.

    public static String FONTESPAGER = "FontePager";
    public static String WEIGHT = "Weight";
    public static String PROFILE = "Profile";
    public static String BODYTRACKING = "BodyTracking";
    public static String BODYTRACKINGDETAILS = "BodyTrackingDetail";
    public static String SETTINGS = "Settings";
    public static String FOOD = "DAOFood";
    public static String MACHINES = "Machines";
    public static String MACHINESDETAILS = "MachinesDetails";
    public static String WORKOUTS = "Workouts";
    public static String WORKOUTPAGER = "WorkoutPager";
    public static String PREFS_NAME = "prefsfile";
    private final int REQUEST_CODE_INTRO = 111;


    private static final int IMPORT_DATABASE = 2;
    public static final int OPEN_MUSIC_FILE = 3;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    Button resendCode;
    Button resetPassLocal,changeProfileImage;
    FirebaseUser user;
    ImageView profileImage;
    StorageReference storageReference;

    private final MusicController musicController = new MusicController(this);
    CustomDrawerAdapter mDrawerAdapter;
    List<DrawerItem> dataList;
    /* Fragments */
    private FontesPagerFragment mpFontesPagerFrag = null;
    private WeightFragment mpWeightFrag = null;
    private ProfileFragment mpProfileFrag = null;
    private MachineFragment mpMachineFrag = null;
    private SettingsFragment mpSettingFrag = null;
    private BodyPartListFragment mpBodyPartListFrag = null;
    private ProgramListFragment mpWorkoutListFrag;
    private String currentFragmentName = "";
    private DAOProfile mDbProfils = null;
    private Profile mCurrentProfile = null;
    private long mCurrentProfilID = -1;
    private Toolbar top_toolbar = null;
    /* Navigation Drawer */
    private DrawerLayout mDrawerLayout = null;
    private ListView mDrawerList = null;
    private ActionBarDrawerToggle mDrawerToggle = null;
    private CircularImageView roundProfile = null;
    private String mCurrentMachine = "";
    private boolean mIntro014Launched = false;
    private boolean mMigrationBD15done = false;
    private boolean mMigrationToScopedStoragedone = false;
    private long mBackPressed;
    private AppViMo appViMo;



    private final PopupMenu.OnMenuItemClickListener onMenuItemClick = item -> {
        switch (item.getItemId()) {
            case R.id.create_newprofil:
                getActivity().CreateNewProfile();
                return true;
            case R.id.change_profil:
                String[] profilListArray = getActivity().mDbProfils.getAllProfile();

                AlertDialog.Builder changeProfilbuilder = new AlertDialog.Builder(getActivity());
                changeProfilbuilder.setTitle(getActivity().getResources().getText(R.string.profil_select_profil))
                        .setItems(profilListArray, (dialog, which) -> {
                            ListView lv = ((AlertDialog) dialog).getListView();
                            Object checkedItem = lv.getAdapter().getItem(which);
                            setCurrentProfile(checkedItem.toString());
                            KToast.infoToast(getActivity(), getActivity().getResources().getText(R.string.profileSelected) + " : " + checkedItem.toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                            //Toast.makeText(getApplicationContext(), getActivity().getResources().getText(R.string.profileSelected) + " : " + checkedItem.toString(), Toast.LENGTH_LONG).show();
                        });
                changeProfilbuilder.show();
                return true;
            case R.id.delete_profil:
                String[] profildeleteListArray = getActivity().mDbProfils.getAllProfile();

                AlertDialog.Builder deleteProfilbuilder = new AlertDialog.Builder(getActivity());
                deleteProfilbuilder.setTitle(getActivity().getResources().getText(R.string.profil_select_profil_to_delete))
                        .setItems(profildeleteListArray, (dialog, which) -> {
                            ListView lv = ((AlertDialog) dialog).getListView();
                            Object checkedItem = lv.getAdapter().getItem(which);
                            if (getCurrentProfile().getName().equals(checkedItem.toString())) {
                                KToast.errorToast(getActivity(), getActivity().getResources().getText(R.string.impossibleToDeleteProfile).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                            } else {
                                Profile profileToDelete = mDbProfils.getProfile(checkedItem.toString());
                                mDbProfils.deleteProfile(profileToDelete);
                                KToast.infoToast(getActivity(), getString(R.string.profileDeleted) + ":" + checkedItem.toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                            }
                        });
                deleteProfilbuilder.show();
                return true;
            case R.id.rename_profil:
                getActivity().renameProfil();
                return true;
            case R.id.param_profil:
                showFragment(PROFILE);
                return true;
            default:
                return false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String dayNightAuto = SP.getString("dayNightAuto", "2");
        int dayNightAutoValue;
        try {
            dayNightAutoValue = Integer.parseInt(dayNightAuto);
        } catch (NumberFormatException e) {
            dayNightAutoValue = 2;
        }
        if (dayNightAutoValue == getResources().getInteger(R.integer.dark_mode_value)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            SweetAlertDialog.DARK_STYLE = true;
        } else if (dayNightAutoValue == getResources().getInteger(R.integer.light_mode_value)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            SweetAlertDialog.DARK_STYLE = false;
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            int currentNightMode = getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            switch (currentNightMode) {
                case Configuration.UI_MODE_NIGHT_YES:
                    SweetAlertDialog.DARK_STYLE = true;
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                default:
                    SweetAlertDialog.DARK_STYLE = false;
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadPreferences();

        top_toolbar = this.findViewById(R.id.actionToolbar);
        setSupportActionBar(top_toolbar);
        top_toolbar.setTitle(getResources().getText(R.string.app_name));

        if (savedInstanceState == null) {
            if (mpFontesPagerFrag == null)
                mpFontesPagerFrag = FontesPagerFragment.newInstance(FONTESPAGER, 6);
            if (mpWeightFrag == null) mpWeightFrag = WeightFragment.newInstance(WEIGHT, 5);
            if (mpProfileFrag == null) mpProfileFrag = ProfileFragment.newInstance(PROFILE, 10);
            if (mpSettingFrag == null) mpSettingFrag = SettingsFragment.newInstance(SETTINGS, 8);
            if (mpMachineFrag == null) mpMachineFrag = MachineFragment.newInstance(MACHINES, 7);
            if (mpBodyPartListFrag == null)
                mpBodyPartListFrag = BodyPartListFragment.newInstance(BODYTRACKING, 9);
            if (mpWorkoutListFrag == null)
                mpWorkoutListFrag = ProgramListFragment.newInstance(WORKOUTS, 11);
        } else {
            mpFontesPagerFrag = (FontesPagerFragment) getSupportFragmentManager().getFragment(savedInstanceState, FONTESPAGER);
            mpWeightFrag = (WeightFragment) getSupportFragmentManager().getFragment(savedInstanceState, WEIGHT);
            mpProfileFrag = (ProfileFragment) getSupportFragmentManager().getFragment(savedInstanceState, PROFILE);
            mpSettingFrag = (SettingsFragment) getSupportFragmentManager().getFragment(savedInstanceState, SETTINGS);
            mpMachineFrag = (MachineFragment) getSupportFragmentManager().getFragment(savedInstanceState, MACHINES);
            mpBodyPartListFrag = (BodyPartListFragment) getSupportFragmentManager().getFragment(savedInstanceState, BODYTRACKING);
            mpWorkoutListFrag = (ProgramListFragment) getSupportFragmentManager().getFragment(savedInstanceState, WORKOUTS);
        }

        appViMo = new ViewModelProvider(this).get(AppViMo.class);
        appViMo.getProfile().observe(this, profile -> {
            // Update UI
            setDrawerTitle(profile.getName());
            setPhotoProfile(profile.getPhoto());
            mCurrentProfilID = profile.getId();
            savePreferences();
        });

        DatabaseHelper.renameOldDatabase(this);

        if (DatabaseHelper.DATABASE_VERSION >= 15 && !mMigrationBD15done) {
            DAOOldCardio mDbOldCardio = new DAOOldCardio(this);
            DAOMachine lDAOMachine = new DAOMachine(this);
            if (mDbOldCardio.tableExists()) {
                DAOCardio mDbCardio = new DAOCardio(this);
                List<OldCardio> mList = mDbOldCardio.getAllRecords();
                for (OldCardio record : mList) {
                    Machine m = lDAOMachine.getMachine(record.getExercice());
                    String exerciseName = record.getExercice();
                    if (m != null) { // if a machine exists
                        if (m.getType() == ExerciseType.STRENGTH) { // if it is not a Cardio type
                            exerciseName = exerciseName + "-Cardio"; // add a suffix to
                        }
                    }

                    mDbCardio.addCardioRecordToFreeWorkout(record.getDate(), exerciseName, record.getDistance(), record.getDuration(), record.getProfil().getId(), DistanceUnit.KM);
                }
                mDbOldCardio.dropTable();

                DAORecord daoRecord = new DAORecord(this);
                List<Record> mFonteList = daoRecord.getAllRecords();
                for (Record record : mFonteList) {
                    record.setExerciseType(ExerciseType.STRENGTH);
                    daoRecord.updateRecord(record); // Automatically update record Type
                }
                List<Machine> machineList = lDAOMachine.getAllMachinesArray();
                for (Machine record : machineList) {
                    lDAOMachine.updateMachine(record); // Reset all the fields on machines.
                }
            }
            mMigrationBD15done = true;
            savePreferences();
        }



        if (savedInstanceState == null) {
            showFragment(FONTESPAGER); // Create fragment, do not add to backstack
            currentFragmentName = FONTESPAGER;
        }

        dataList = new ArrayList<>();
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.left_drawer);

        DrawerItem drawerTitleItem = new DrawerItem("TITLE", R.drawable.ic_person, true);

        dataList.add(drawerTitleItem);
        dataList.add(new DrawerItem(this.getResources().getString(R.string.menu_Workout), R.drawable.ic_fitness_center, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.MachinesLabel), R.drawable.ic_exercises, true));
        dataList.add(new DrawerItem("Programs List", R.drawable.ic_exam, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.weightMenuLabel), R.drawable.ic_bathroom_scale, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.bodytracking), R.drawable.ic_ruler, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.SettingLabel), R.drawable.ic_settings, true));

        mDrawerAdapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item,
                dataList);

        mDrawerList.setAdapter(mDrawerAdapter);

        roundProfile = top_toolbar.findViewById(R.id.imageProfile);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                top_toolbar,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open, R.string.drawer_close
        );

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        musicController.initView();

        // Test if intro has been launched already
        if (!mIntro014Launched) {
            Intent intent = new Intent(this, MainIntroActivity.class);
            startActivityForResult(intent, REQUEST_CODE_INTRO);
        }


    }


    @Override
    protected void onStart() {
        super.onStart();  // Always call the superclass method first


        if (mIntro014Launched) {
            initActivity();
            initDEBUGdata();
        }


        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean bShowMP3 = SP.getBoolean("prefShowMP3", false);
        this.showMP3Toolbar(bShowMP3);
    }



    private void initDEBUGdata() {
        if (BuildConfig.DEBUG_MODE) {
            // do something for a debug build
            DAOFonte lDbFonte = new DAOFonte(this);
            if (lDbFonte.getCount() == 0) {
                lDbFonte.addStrengthRecordToFreeWorkout(DateConverter.dateToDate(2019, 7, 1, 12, 34, 56), "Example 1", 1, 10, 40, WeightUnit.KG, "", this.getCurrentProfile().getId());
                lDbFonte.addStrengthRecordToFreeWorkout(DateConverter.dateToDate(2019, 6, 30, 12, 34, 56), "Example 2", 1, 10, UnitConverter.LbstoKg(60), WeightUnit.LBS, "", this.getCurrentProfile().getId());
            }
            DAOCardio lDbCardio = new DAOCardio(this);
            if (lDbCardio.getCount() == 0) {
                lDbCardio.addCardioRecordToFreeWorkout(DateConverter.dateToDate(2019, 7, 1), "Running Example", 1, 10000, this.getCurrentProfile().getId(), DistanceUnit.KM);
                lDbCardio.addCardioRecordToFreeWorkout(DateConverter.dateToDate(2019, 7, 31), "Cardio Example", UnitConverter.MilesToKm(2), 20000, this.getCurrentProfile().getId(), DistanceUnit.MILES);
            }
            DAOStatic lDbStatic = new DAOStatic(this);
            if (lDbStatic.getCount() == 0) {
                lDbStatic.addStaticRecordToFreeWorkout(DateConverter.dateToDate(2019, 7, 1, 12, 34, 56), "Exercise ISO 1", 1, 50, 40, this.getCurrentProfile().getId(), WeightUnit.KG, "");
                lDbStatic.addStaticRecordToFreeWorkout(DateConverter.dateToDate(2019, 7, 31, 12, 34, 56), "Exercise ISO 2", 1, 60, UnitConverter.LbstoKg(40), this.getCurrentProfile().getId(), WeightUnit.LBS, "");
            }
            DAOProgram lDbWorkout = new DAOProgram(this);
            if (lDbWorkout.getCount() == 0) {
                lDbWorkout.populate();
            }
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        if (getFontesPagerFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, FONTESPAGER, mpFontesPagerFrag);
        if (getWeightFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, WEIGHT, mpWeightFrag);
        if (getProfileFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, PROFILE, mpProfileFrag);
        if (getMachineFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, MACHINES, mpMachineFrag);
        if (getSettingsFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, SETTINGS, mpSettingFrag);
        if (getBodyPartFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, BODYTRACKING, mpBodyPartListFrag);
        if (getWorkoutListFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, WORKOUTS, mpWorkoutListFrag);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);

        // restore the profile picture in case it was overwritten during the menu inflate
        if (mCurrentProfile != null) setPhotoProfile(mCurrentProfile.getPhoto());

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        roundProfile.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getActivity(), v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.profile_actions, popup.getMenu());
            popup.setOnMenuItemClickListener(onMenuItemClick);
            popup.show();
        });

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_chrono:
                ChronoDialogbox cdd = new ChronoDialogbox(MainActivity.this);
                cdd.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


        public boolean CreateNewProfile() {
        AlertDialog.Builder newProfilBuilder = new AlertDialog.Builder(this);

        newProfilBuilder.setTitle(getActivity().getResources().getText(R.string.createProfilTitle));
        newProfilBuilder.setMessage(getActivity().getResources().getText(R.string.createProfilQuestion));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        newProfilBuilder.setView(input);

        newProfilBuilder.setPositiveButton(getActivity().getResources().getText(android.R.string.ok), (dialog, whichButton) -> {
            String value = input.getText().toString();

            if (value.isEmpty()) {
                CreateNewProfile();
            } else {
                // Create the new profil
                mDbProfils.addProfile(value);
                // Make it the current.
                setCurrentProfile(value);
            }
        });

        newProfilBuilder.setNegativeButton(getActivity().getResources().getText(android.R.string.cancel), (dialog, whichButton) -> {
            if (getCurrentProfile() == null) {
                CreateNewProfile();
            }
        });

        newProfilBuilder.show();

        return true;
    }

    public boolean renameProfil() {
        AlertDialog.Builder newBuilder = new AlertDialog.Builder(this);

        newBuilder.setTitle(getActivity().getResources().getText(R.string.renameProfilTitle));
        newBuilder.setMessage(getActivity().getResources().getText(R.string.renameProfilQuestion));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setText(getCurrentProfile().getName());
        newBuilder.setView(input);

        newBuilder.setPositiveButton(getActivity().getResources().getText(android.R.string.ok), (dialog, whichButton) -> {
            String value = input.getText().toString();

            if (!value.isEmpty()) {
                // Get current profile
                Profile temp = getCurrentProfile();
                // Rename it
                temp.setName(value);
                // Commit it
                mDbProfils.updateProfile(temp);
                // Make it the current.
                setCurrentProfile(value);
            }
        });

        newBuilder.setNegativeButton(getActivity().getResources().getText(android.R.string.cancel), (dialog, whichButton) -> {
        });

        newBuilder.show();

        return true;
    }

    private void setDrawerTitle(String pProfilName) {
        mDrawerAdapter.getItem(0).setTitle(pProfilName);
        mDrawerAdapter.notifyDataSetChanged();
        mDrawerLayout.invalidate();
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {
        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        //setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void showFragment(String pFragmentName) {

        if (currentFragmentName.equals(pFragmentName))
            return; // If this is already the current fragment, do no replace.

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        // Then show the fragments
        if (pFragmentName.equals(FONTESPAGER)) {
            ft.replace(R.id.fragment_container, getFontesPagerFragment(), FONTESPAGER);
        } else if (pFragmentName.equals(WEIGHT)) {
            ft.replace(R.id.fragment_container, getWeightFragment(), WEIGHT);
        } else if (pFragmentName.equals(SETTINGS)) {
            ft.replace(R.id.fragment_container, getSettingsFragment(), SETTINGS);
        } else if (pFragmentName.equals(MACHINES)) {
            ft.replace(R.id.fragment_container, getMachineFragment(), MACHINES);
        } else if (pFragmentName.equals(WORKOUTS)) {
            ft.replace(R.id.fragment_container, getWorkoutListFragment(), WORKOUTS);
        } else if (pFragmentName.equals(BODYTRACKING)) {
            ft.replace(R.id.fragment_container, getBodyPartFragment(), BODYTRACKING);
        } else if (pFragmentName.equals(PROFILE)) {
            ft.replace(R.id.fragment_container, getProfileFragment(), PROFILE);
        }
        currentFragmentName = pFragmentName;
        ft.commit();

    }

    @Override
    protected void onStop() {
        super.onStop();
        savePreferences();
    }

    public Profile getCurrentProfile() {
        return appViMo.getProfile().getValue();
    }

    public void setCurrentProfile(String newProfileName) {
        Profile newProfile = this.mDbProfils.getProfile(newProfileName);
        setCurrentProfile(newProfile);
    }

    public void setCurrentProfile(Profile newProfil) {
        appViMo.setProfile(newProfil);
    }

    private void setPhotoProfile(String path) {
        ImageUtil imgUtil = new ImageUtil();

        // Check if path is pointing to a thumb else create it and use it.
        String thumbPath = imgUtil.getThumbPath(path);
        boolean success = false;
        if (thumbPath != null) {
            success = ImageUtil.setPic(roundProfile, thumbPath);
            mDrawerAdapter.getItem(0).setImg(thumbPath);
        }

        if (!success) {
            roundProfile.setImageDrawable(AppCompatResources.getDrawable(getActivity(), R.drawable.ic_person));
            mDrawerAdapter.getItem(0).setImg(null); // Img has priority over Resource so it needs to be reset
            mDrawerAdapter.getItem(0).setImgResID(R.drawable.ic_person);
        }

        mDrawerAdapter.notifyDataSetChanged();
        mDrawerLayout.invalidate();
    }

    public String getCurrentMachine() {
        return mCurrentMachine;
    }

    public void setCurrentMachine(String newMachine) {
        mCurrentMachine = newMachine;
    }

    public MainActivity getActivity() {
        return this;
    }

    private void loadPreferences() {
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mCurrentProfilID = settings.getLong("currentProfil", -1); // return -1 if it doesn't exist
        mIntro014Launched = settings.getBoolean("intro014Launched", false);
        mMigrationBD15done = settings.getBoolean("migrationBD15done", false);
        mMigrationToScopedStoragedone = settings.getBoolean("migrationToScopedStoragedone", false);
    }

    private void savePreferences() {
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("currentProfil", mCurrentProfilID);
        editor.putBoolean("intro014Launched", mIntro014Launched);
        editor.putBoolean("migrationBD15done", mMigrationBD15done);
        editor.putBoolean("migrationToScopedStoragedone", mMigrationToScopedStoragedone);
        editor.apply();
    }

    private FontesPagerFragment getFontesPagerFragment() {
        if (mpFontesPagerFrag == null)
            mpFontesPagerFrag = (FontesPagerFragment) getSupportFragmentManager().findFragmentByTag(FONTESPAGER);
        if (mpFontesPagerFrag == null)
            mpFontesPagerFrag = FontesPagerFragment.newInstance(FONTESPAGER, 6);

        return mpFontesPagerFrag;
    }

    private WeightFragment getWeightFragment() {
        if (mpWeightFrag == null)
            mpWeightFrag = (WeightFragment) getSupportFragmentManager().findFragmentByTag(WEIGHT);
        if (mpWeightFrag == null) mpWeightFrag = WeightFragment.newInstance(WEIGHT, 5);

        return mpWeightFrag;
    }

    private ProfileFragment getProfileFragment() {
        if (mpProfileFrag == null)
            mpProfileFrag = (ProfileFragment) getSupportFragmentManager().findFragmentByTag(PROFILE);
        if (mpProfileFrag == null) mpProfileFrag = ProfileFragment.newInstance(PROFILE, 10);

        return mpProfileFrag;
    }

    private MachineFragment getMachineFragment() {
        if (mpMachineFrag == null)
            mpMachineFrag = (MachineFragment) getSupportFragmentManager().findFragmentByTag(MACHINES);
        if (mpMachineFrag == null) mpMachineFrag = MachineFragment.newInstance(MACHINES, 7);
        return mpMachineFrag;
    }


    private BodyPartListFragment getBodyPartFragment() {
        if (mpBodyPartListFrag == null)
            mpBodyPartListFrag = (BodyPartListFragment) getSupportFragmentManager().findFragmentByTag(BODYTRACKING);
        if (mpBodyPartListFrag == null)
            mpBodyPartListFrag = BodyPartListFragment.newInstance(BODYTRACKING, 9);

        return mpBodyPartListFrag;
    }


    private ProgramListFragment getWorkoutListFragment() {
        if (mpWorkoutListFrag == null)
            mpWorkoutListFrag = (ProgramListFragment) getSupportFragmentManager().findFragmentByTag(WORKOUTS);
        if (mpWorkoutListFrag == null)
            mpWorkoutListFrag = ProgramListFragment.newInstance(WORKOUTS, 10);

        return mpWorkoutListFrag;
    }

    private SettingsFragment getSettingsFragment() {
        if (mpSettingFrag == null)
            mpSettingFrag = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(SETTINGS);
        if (mpSettingFrag == null) mpSettingFrag = SettingsFragment.newInstance(SETTINGS, 8);

        return mpSettingFrag;
    }

    public Toolbar getActivityToolbar() {
        return top_toolbar;
    }

    public void showMP3Toolbar(boolean show) {
        Toolbar mp3toolbar = this.findViewById(R.id.musicToolbar);
        if (!show) {
            mp3toolbar.setVisibility(View.GONE);
        } else {
            mp3toolbar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_INTRO) {
            if (resultCode == RESULT_OK) {
                initActivity();
                mIntro014Launched = true;
                initDEBUGdata();
                this.savePreferences();
            } else {
                // Cancelled the intro. You can then e.g. finish this activity too.
                finish();
            }
        } else if (resultCode == RESULT_OK && requestCode == IMPORT_DATABASE) {
            Uri file;
            if (data != null) {
                file = data.getData();
                InputStream inputStream;
                try {
                    inputStream = getContentResolver().openInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    inputStream = null;
                }

            }
        } else if (resultCode == RESULT_OK && requestCode == OPEN_MUSIC_FILE) {
            Uri uri;
            if (data != null) {
                uri = data.getData();
                // Return for MusicController Choose file
                musicController.OpenMusicFileIntentResult(uri);
                int takeFlags = data.getFlags();
                takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                // Check for the freshest data.
                getContentResolver().takePersistableUriPermission(uri, takeFlags);
            }
        }
    }

    @Override
    public void onBackPressed() {
        int index = getActivity().getSupportFragmentManager().getBackStackEntryCount() - 1;
        if (index >= 0) { // Si on est dans une sous activité
            super.onBackPressed();
            getActivity().getSupportActionBar().show();
        } else { // Si on est la racine, avec il faut cliquer deux fois
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                super.onBackPressed();
                return;
            } else {
                Toast.makeText(getBaseContext(), R.string.pressBackAgain, Toast.LENGTH_SHORT).show();
            }

            mBackPressed = System.currentTimeMillis();
        }
    }

    public void initActivity() {
        // Initialisation des objets DB
        mDbProfils = new DAOProfile(this.getApplicationContext());

        // Pour la base de donnee profil, il faut toujours qu'il y ai au moins un profil
        mCurrentProfile = mDbProfils.getProfile(mCurrentProfilID);
        if (mCurrentProfile == null) { // au cas ou il y aurait un probleme de synchro
            try {
                List<Profile> lList = mDbProfils.getAllProfiles(mDbProfils.getReadableDatabase());
                mCurrentProfile = lList.get(0);
            } catch (IndexOutOfBoundsException e) {
                this.CreateNewProfile();
            }
        }

        if (mCurrentProfile != null) setCurrentProfile(mCurrentProfile.getName());

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {

            selectItem(position);

            // Insert the fragment by replacing any existing fragment
            switch (position) {
                case 0:
                    showFragment(PROFILE);
                    setTitle(getString(R.string.ProfileLabel));
                    break;
                case 1:
                    showFragment(FONTESPAGER);
                    setTitle(getResources().getText(R.string.menu_Workout));
                    break;
                case 2:
                    showFragment(MACHINES);
                    setTitle(getResources().getText(R.string.MachinesLabel));
                    break;
                case 3:
                    showFragment(WORKOUTS);
                    setTitle(getString(R.string.workout_list_menu_item));
                    break;
                case 4:
                    showFragment(WEIGHT);
                    setTitle(getResources().getText(R.string.weightMenuLabel));
                    break;
                case 5:
                    showFragment(BODYTRACKING);
                    setTitle(getResources().getText(R.string.bodytracking));
                    break;
                case 6:
                    showFragment(SETTINGS);
                    setTitle(getResources().getText(R.string.SettingLabel));
                    break;
                case 7:
                    showFragment(FOOD);
                    setTitle(getResources().getText(R.string.Food));
                    break;
                default:
                    showFragment(FONTESPAGER);
                    setTitle(getResources().getText(R.string.FonteLabel));
            }
        }
    }
}
