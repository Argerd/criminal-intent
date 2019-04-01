package argerd.ru.database;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import argerd.ru.R;

public class DialogFragmentPhoto extends DialogFragment {
    private static final String ARG_PATH = "path";

    ImageView photoImage;

    public static DialogFragmentPhoto newInstance(String pathFile) {
        Bundle args = new Bundle();
        args.putString(ARG_PATH, pathFile);

        DialogFragmentPhoto dialogFragmentPhoto = new DialogFragmentPhoto();
        dialogFragmentPhoto.setArguments(args);
        return dialogFragmentPhoto;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(R.string.dialog_fragment_photo);

        View view = inflater.inflate(R.layout.dialog_fragment_photo, null);
        photoImage = view.findViewById(R.id.dialog_fragment_image_photo);
        try {
            photoImage.setImageBitmap(BitmapFactory.decodeFile(getArguments().getString(ARG_PATH)));
        } catch (Exception e) {
            System.out.println(e);
        }

        return view;
    }
}
