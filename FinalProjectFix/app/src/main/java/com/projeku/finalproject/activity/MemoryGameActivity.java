package com.projeku.finalproject.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.projeku.finalproject.R;
import com.projeku.finalproject.databinding.ActivityMemoryGameBinding;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryGameActivity extends AppCompatActivity {

    private ActivityMemoryGameBinding binding;
    private List<Integer> cardImages;
    private List<ImageView> cards;
    private int firstCard = -1;
    private int secondCard = -1;
    private boolean isBusy = false;
    private int matchedPairs = 0;
    private int moves = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemoryGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupGame();

        binding.buttonNewGame.setOnClickListener(v -> resetGame());
    }

    private void setupToolbar() {
        binding.toolbarGame.setNavigationOnClickListener(v -> checkForExitConfirmation());
    }

    private void checkForExitConfirmation() {
        if (matchedPairs > 0 && matchedPairs < 6) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.quit_game)
                    .setMessage(R.string.quit_game_message)
                    .setPositiveButton(R.string.quit, (dialog, which) -> finish())
                    .setNegativeButton(R.string.keep_playing, null)
                    .show();
        } else {
            finish();
        }
    }

    private void setupGame() {
        // Initialize card images (pairs of images)
        cardImages = new ArrayList<>();
        // Add pairs of cards (6 pairs = 12 cards)
        cardImages.add(R.drawable.card_1);
        cardImages.add(R.drawable.card_1);
        cardImages.add(R.drawable.card_2);
        cardImages.add(R.drawable.card_2);
        cardImages.add(R.drawable.card_3);
        cardImages.add(R.drawable.card_3);
        cardImages.add(R.drawable.card_4);
        cardImages.add(R.drawable.card_4);
        cardImages.add(R.drawable.card_5);
        cardImages.add(R.drawable.card_5);
        cardImages.add(R.drawable.card_6);
        cardImages.add(R.drawable.card_6);

        // Shuffle the cards
        Collections.shuffle(cardImages);

        // Setup the grid with cards
        cards = new ArrayList<>();
        GridLayout gridLayout = binding.gridLayoutCards;
        gridLayout.removeAllViews();

        for (int i = 0; i < cardImages.size(); i++) {
            ImageView card = new ImageView(this);
            card.setId(View.generateViewId());
            cards.add(card);

            // Set card dimensions
            int cardSize = getResources().getDimensionPixelSize(R.dimen.card_size);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = cardSize;
            params.height = cardSize;
            params.setMargins(8, 8, 8, 8);

            // Set row and column
            params.rowSpec = GridLayout.spec(i / 4);
            params.columnSpec = GridLayout.spec(i % 4);

            card.setLayoutParams(params);
            card.setBackgroundResource(R.drawable.card_back);
            card.setScaleType(ImageView.ScaleType.CENTER_CROP);

            final int cardIndex = i;
            card.setOnClickListener(v -> onCardClick(cardIndex));

            gridLayout.addView(card);
        }

        // Reset game state
        matchedPairs = 0;
        moves = 0;
        updateMovesText();
    }

    private void onCardClick(int index) {
        // Ignore clicks if busy or card already matched
        if (isBusy || index == firstCard || cards.get(index).getAlpha() < 1.0f) {
            return;
        }

        // Flip the card
        ImageView card = cards.get(index);
        card.setImageResource(cardImages.get(index));

        // Handle first and second card selections
        if (firstCard == -1) {
            // First card selected
            firstCard = index;
        } else {
            // Second card selected
            secondCard = index;
            checkForMatch();
        }
    }

    private void checkForMatch() {
        moves++;
        updateMovesText();

        isBusy = true;
        new Handler().postDelayed(() -> {
            // Check if the two cards match
            if (cardImages.get(firstCard).equals(cardImages.get(secondCard))) {
                // Cards match - keep them flipped but fade them
                cards.get(firstCard).setAlpha(0.7f);
                cards.get(secondCard).setAlpha(0.7f);
                matchedPairs++;

                // Check if game is complete
                if (matchedPairs == cardImages.size() / 2) {
                    showGameCompleteDialog();
                }
            } else {
                // Cards don't match - flip them back
                cards.get(firstCard).setImageResource(0);
                cards.get(secondCard).setImageResource(0);
                cards.get(firstCard).setBackgroundResource(R.drawable.card_back);
                cards.get(secondCard).setBackgroundResource(R.drawable.card_back);
            }

            // Reset for next turn
            firstCard = -1;
            secondCard = -1;
            isBusy = false;
        }, 800); // Delay before hiding non-matching cards
    }

    private void updateMovesText() {
        binding.textViewMoves.setText(getString(R.string.moves_count, moves));
    }

    private void showGameCompleteDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.congratulations)
                .setMessage(getString(R.string.game_complete_message, moves))
                .setPositiveButton(R.string.play_again, (dialog, which) -> resetGame())
                .setNegativeButton(R.string.quit, (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void resetGame() {
        // Shuffle cards again
        Collections.shuffle(cardImages);

        // Reset all cards
        for (ImageView card : cards) {
            card.setImageResource(0);
            card.setBackgroundResource(R.drawable.card_back);
            card.setAlpha(1.0f);
        }

        // Reset game state
        firstCard = -1;
        secondCard = -1;
        isBusy = false;
        matchedPairs = 0;
        moves = 0;
        updateMovesText();
    }

    @Override
    public void onBackPressed() {
        checkForExitConfirmation();
        super.onBackPressed();
    }
}