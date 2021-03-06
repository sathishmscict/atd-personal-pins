package com.ang.acb.personalpins.ui.pins;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ang.acb.personalpins.R;
import com.ang.acb.personalpins.data.entity.Pin;
import com.ang.acb.personalpins.databinding.FragmentFavoritePinsBinding;
import com.ang.acb.personalpins.utils.GridMarginDecoration;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

import static com.ang.acb.personalpins.ui.pins.PinDetailsFragment.ARG_PIN_ID;
import static com.ang.acb.personalpins.ui.pins.PinDetailsFragment.ARG_PIN_IS_PHOTO;

public class FavoritePinsFragment extends Fragment {

    private FragmentFavoritePinsBinding binding;
    private PinsViewModel pinsViewModel;
    private PinsAdapter pinsAdapter;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    // Required empty public constructor
    public FavoritePinsFragment() {}

    @Override
    public void onAttach(@NotNull Context context) {
        // When using Dagger for injecting Fragments, inject as early as possible.
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment and get an instance of the binding class.
        binding = FragmentFavoritePinsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initViewModel();
        initAdapter();
        populateUi();
    }

    private void initViewModel() {
        pinsViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(PinsViewModel.class);
    }

    private void initAdapter() {
        pinsAdapter = new PinsAdapter(this::onPinClick);
        binding.favorites.rv.setLayoutManager(new GridLayoutManager(
                getContext(), getResources().getInteger(R.integer.columns_count)));
        binding.favorites.rv.addItemDecoration(new GridMarginDecoration(
                getContext(), R.dimen.grid_item_spacing));
        binding.favorites.rv.setAdapter(pinsAdapter);
    }

    private void onPinClick(Pin pin, ImageView sharedImage) {
        // On item click navigate to pin details fragment
        Bundle args = new Bundle();
        args.putLong(ARG_PIN_ID, pin.getId());
        if (pin.getPhotoUri() != null && sharedImage != null) {
            // This is a photo pin
            args.putBoolean(ARG_PIN_IS_PHOTO, true);
            // Create the shared element transition extras.
            FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                    .addSharedElement(sharedImage, sharedImage.getTransitionName())
                    .build();
            NavHostFragment.findNavController(FavoritePinsFragment.this)
                    .navigate(R.id.action_favorite_pins_to_pin_details,
                              args, null, extras);
        } else {
            // This is a video pin, there are no shared element transition extras.
            args.putBoolean(ARG_PIN_IS_PHOTO, false);
            NavHostFragment.findNavController(FavoritePinsFragment.this)
                    .navigate(R.id.action_favorite_pins_to_pin_details, args);
        }
    }

    private void populateUi() {
        pinsViewModel.getAllFavoritePins().observe(getViewLifecycleOwner(), pins -> {
            int favoritesCount = (pins == null) ? 0 : pins.size();
            binding.setFavoritesCount(favoritesCount);

            if(favoritesCount != 0) pinsAdapter.submitList(pins);
            else binding.favoritesEmptyState.tv.setText(R.string.no_favorites);

            binding.executePendingBindings();
        });
    }
}
