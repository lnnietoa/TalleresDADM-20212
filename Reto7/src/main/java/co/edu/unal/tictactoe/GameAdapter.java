package co.edu.unal.tictactoe;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.ViewHolder> {

    private static String uuidPlayer;
    private static List<Game> games;
    private static List<String> keys;

    public GameAdapter(String uuidPlayer, List<Game> games, List<String> keys) {
        this.uuidPlayer = uuidPlayer;
        this.games = games;
        this.keys = keys;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_multiplayer, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String name = games.get(position).uuidDefendingPlayer;
        holder.name.setText(name);
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView mInfoTextView;

        public ViewHolder(final View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.name_game);

            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(v.getContext(), AndroidTicTacToeActivity.class);
                    intent.putExtra("uuidPlayer", uuidPlayer);
                    intent.putExtra("keyGame", keys.get(getLayoutPosition()));
                    String challengerPlayer = games.get(getLayoutPosition()).uuidChallengingPlayer;
                    String defendingPlayer = games.get(getLayoutPosition()).uuidDefendingPlayer;

                    if(uuidPlayer.equals(defendingPlayer)) {
                        intent.putExtra("isChallengingPlayer", "O");
                    }
                    else if(challengerPlayer == null || uuidPlayer.equals(challengerPlayer)){
                        intent.putExtra("isChallengingPlayer", "1");

                    }else {
                        intent.putExtra("isChallengingPlayer", "2");
                        intent.putExtra("defendingPlayer", defendingPlayer );
                        intent.putExtra("challengerPlayer", challengerPlayer );

                    }


                    v.getContext().startActivity(intent);
                }
            });
        }
    }
}