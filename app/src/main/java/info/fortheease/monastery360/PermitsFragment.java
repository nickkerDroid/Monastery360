package info.fortheease.monastery360;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PermitsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_permits, container, false); // Changed here
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView linkILP = view.findViewById(R.id.linkILP);
        TextView linkPAP = view.findViewById(R.id.linkPAP);

        setLinkClick(linkILP, "https://sikkimiptourism.gov.in/");
        setLinkClick(linkPAP, "https://sikkimtourism.gov.in/GeneralPages/RestrictedAreaPermit");
    }

    private void setLinkClick(TextView textView, final String url) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                if (getActivity() != null) {
                    getActivity().startActivity(browserIntent);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setMonasterySpinnerEnabled(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setMonasterySpinnerEnabled(true);
        }
    }
}
