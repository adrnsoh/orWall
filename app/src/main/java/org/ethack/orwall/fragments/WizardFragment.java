package org.ethack.orwall.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import org.ethack.orwall.R;
import org.ethack.orwall.iptables.InitializeIptables;
import org.ethack.orwall.lib.Constants;
import org.ethack.orwall.lib.InstallScripts;
import org.sufficientlysecure.rootcommands.RootCommands;

import info.guardianproject.onionkit.ui.OrbotHelper;

/**
 * A simple {@link Fragment} subclass.
 * Will display a simple Wizard explaining User what orWall can do.
 */
public class WizardFragment extends Fragment {

    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE = "page";

    /**
     * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
     */
    private int mPageNumber;


    public WizardFragment() {
    }

    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static WizardFragment create(int position) {
        WizardFragment fragment = new WizardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_wizard, container, false);

        int[] titles = {
                R.string.wizard_title_one,
                R.string.wizard_title_two,
                R.string.wizard_title_three,
        };

        String title = getString(R.string.wizard_title_one);
        String step = "Step 1: ";
        if (mPageNumber < titles.length) {
            title = getString(titles[mPageNumber]);
            step = String.format("Step %d: ", mPageNumber + 1);
        }
        ((TextView) rootView.findViewById(R.id.wizard_step_title))
                .setText(step + title);

        int[] fragments = {
                R.string.wizard_first,
                R.string.wizard_second,
                R.string.wizard_third,
        };

        String fragment = getString(fragments[0]);
        if (mPageNumber < fragments.length) {
            fragment = getString(fragments[mPageNumber]);
        }
        ((TextView) rootView.findViewById(R.id.wizard_fragment_content)).setText(fragment);

        rootView.findViewById(R.id.wizard_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean(Constants.PREF_KEY_FIRST_RUN, false).apply();
                getActivity().finish();
            }
        });

        // Add some stuff on the very first Wizard page
        if (mPageNumber == 0) {
            ViewGroup main_content = (ViewGroup) rootView.findViewById(R.id.id_main_content);
            SharedPreferences preferenceManager = getActivity().getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
            final InitializeIptables initializeIptables = new InitializeIptables(getActivity());
            // Extract scripts
            InstallScripts installScripts = new InstallScripts(getActivity());
            installScripts.run();

            // init-script installation
            // install init as default behavior
            initializeIptables.installInitScript();
            boolean enforceInit = preferenceManager.getBoolean(Constants.PREF_KEY_ENFOCE_INIT, true);
            boolean initSupported = initializeIptables.initSupported();

            Switch initScript = new Switch(getActivity());
            initScript.setChecked( (enforceInit && initSupported) );
            initScript.setText(getString(R.string.wizard_init_script_text));
            initScript.setEnabled(initSupported);

            initScript.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    boolean checked = compoundButton.isChecked();
                    if (checked) {
                        initializeIptables.installInitScript();
                    } else {
                        initializeIptables.removeIniScript();
                    }
                }
            });

            main_content.addView(initScript);

            // Root status
            Switch rootStatus = new Switch(getActivity());
            rootStatus.setChecked(RootCommands.rootAccessGiven());
            rootStatus.setEnabled(false);
            rootStatus.setText(getString(R.string.wizard_init_root_text));
            main_content.addView(rootStatus);

            // Does iptables exist?
            Switch iptablesStatus = new Switch(getActivity());
            iptablesStatus.setChecked(initializeIptables.iptablesExists());
            iptablesStatus.setEnabled(false);
            iptablesStatus.setText(getString(R.string.wizard_init_iptables_text));
            main_content.addView(iptablesStatus);

            // Does current kernel support IPTables comments?
            initializeIptables.supportComments();
            Switch iptablesComments = new Switch(getActivity());
            iptablesComments.setChecked(preferenceManager.getBoolean(Constants.CONFIG_IPT_SUPPORTS_COMMENTS, false));
            iptablesComments.setEnabled(false);
            iptablesComments.setText(getString(R.string.wizard_init_ipt_comments_text));
            main_content.addView(iptablesComments);

            // Is orbot installed?
            OrbotHelper orbotHelper = new OrbotHelper(getActivity());
            Switch orbotStatus = new Switch(getActivity());
            orbotStatus.setChecked(orbotHelper.isOrbotInstalled());
            orbotStatus.setEnabled(false);
            orbotStatus.setText(getString(R.string.wizard_orbot_status_text));
            main_content.addView(orbotStatus);

        }

        return rootView;
    }

    /**
     * Returns the page number represented by this fragment object.
     */
    public int getPageNumber() {
        return mPageNumber;
    }

}
