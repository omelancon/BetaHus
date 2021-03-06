package student_player;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import java.util.ArrayList;
import java.util.Random;

import student_player.mytools.MyTools;

/** A Hus player submitted by a student. */
public class StudentPlayer extends HusPlayer {

    /** You must modify this constructor to return your student number.
     * This is important, because this is what the code that runs the
     * competition uses to associate you with your agent.
     * The constructor should do nothing else. */
    public StudentPlayer() { super("260523926"); }

    private int player_id = 0;
    private int depth_of_search = 1;

    // Parameters for board value heuristic
    private double GREED = 1.0; // Value given to a seed
    private double LIBERTY_DESIRE = 1.0;  // Value given to a legal move

    /** This is the primary method that you need to implement.
     * The ``board_state`` object contains the current state of the game,
     * which your agent can use to make decisions. See the class hus.RandomHusPlayer
     * for another example agent. */
    public HusMove chooseMove(HusBoardState board_state)
    {

      if (board_state.getTurnNumber() == 0){

        //System.out.println("BetaHus: This is the first turn, I have 30000ms");

        long thirthy_seconds_counter = System.currentTimeMillis();

        this.player_id = board_state.getTurnPlayer();

        System.out.println("BetaHus: I know I am player " + this.player_id);

        // Estimate the depth that can be reached in 2000 milliseconds.
        // Our target is around 1000 ms
        long time_taken = 0;
        long start_time;
        long stop_time;

        HusMove dummy_move;

        System.out.println("BetaHus: Let's see what I can do on this machine");

        while (true){
          System.out.println("BetaHus: Can I go to depth " + this.depth_of_search + "?");
          HusBoardState random_board = MyTools.get_random_board();
          System.out.println("BetaHus: I generated a random board for the occasion");

          start_time = System.currentTimeMillis();
          dummy_move = alpha_beta_pruning(random_board, this.depth_of_search);
          stop_time = System.currentTimeMillis();
          time_taken = stop_time - start_time;

          System.out.println("BetaHus: It took " + (int)time_taken + "ms to go to depth " + this.depth_of_search);

          if (time_taken > 600 || (stop_time - thirthy_seconds_counter) > 20000) {
            System.out.println("BetaHus: Ok, that's enough trials");
            break;
          }

          this.depth_of_search += 1;

        }

        // We should now be at the limit depth, run some safety tests
        int test_count = 0;

        while (System.currentTimeMillis() - thirthy_seconds_counter < 27000 && test_count < 25){
          if (time_taken > 1200){
            this.depth_of_search -= 1;
            test_count = 0;
          }
          HusBoardState random_board = MyTools.get_random_board();

          start_time = System.currentTimeMillis();
          dummy_move = alpha_beta_pruning(random_board, this.depth_of_search);
          stop_time = System.currentTimeMillis();

          time_taken = stop_time - start_time;
          test_count += 1;
        }

        System.out.println("BetaHus: After " + test_count + " tests I have my depth set at " + this.depth_of_search);
        long time_remaining = 30000 - (System.currentTimeMillis() - thirthy_seconds_counter);
        System.out.println("BetaHus: And I have " + (int)time_remaining + "ms left");

        if (time_taken > 2000){
          // Panic abort, reduce depth drastically.
          System.out.println("BetaHus: A panic abort was necessary in time evaluation");
          this.depth_of_search = this.depth_of_search / 2;
        }

        System.out.println("BetaHus: Evaluated safe depth of search: " + this.depth_of_search);
      }

      HusMove move = alpha_beta_pruning(board_state, this.depth_of_search);

      return move;
    }

    private HusMove alpha_beta_pruning (HusBoardState board_state, int depth){
      /* Applies standard alpha-beta pruning to a given depth and return
       * the best move.
      */

      ArrayList<HusMove> moves = board_state.getLegalMoves();
      double score = Double.MIN_VALUE;
      double move_score;

      HusMove best_move = new HusMove();

      for (HusMove move : moves){
        HusBoardState opponent_board_state = (HusBoardState) board_state.clone();
        opponent_board_state.move(move);

        move_score = get_value_after_player(opponent_board_state, depth, score);

        if (move_score >= score){
          best_move = move;
          score = move_score;
        }
      }

      return best_move;
    }

    private double get_value_after_player(HusBoardState opponent_board_state, int depth, double min){

      ArrayList<HusMove> opponent_moves = opponent_board_state.getLegalMoves();

      double move_score = Double.MAX_VALUE;

      for (HusMove move : opponent_moves) {
        HusBoardState player_board_state = (HusBoardState) opponent_board_state.clone();
        player_board_state.move(move);

        move_score = get_value_after_opponent(player_board_state, depth - 1, move_score);

        if (move_score < min) {
            break;
        }
      }

      return move_score;
    }

    private double get_value_after_opponent(HusBoardState player_board_state, int depth, double max){

      if (depth <= 0) {
          return getBoardValue(player_board_state);
      }

      ArrayList<HusMove> player_moves = player_board_state.getLegalMoves();
      HusBoardState opponent_board_state;

      double move_score = Double.MIN_VALUE;

      for (HusMove move : player_moves) {
        opponent_board_state = (HusBoardState) player_board_state.clone();
        opponent_board_state.move(move);

        move_score = get_value_after_player(player_board_state, depth, move_score);

        if (move_score >= max) {
            break;
        }
      }

      return move_score;
    }

    private double getBoardValue(HusBoardState board_state){
      /** Heuristic evaluating a move strength based on scalable
       *  attack, defense and degree of freedom desires
       */

      // getMoveValue is intended to be an heuristig to determine move strenght

      ArrayList<HusMove> player_moves = board_state.getLegalMoves();

      int[] player_pits = board_state.getPits()[this.player_id];
      int player_seeds = 0;

      // TODO: Check if the pit is the number of seeds and not the index!
      for (int pit : player_pits){
        player_seeds += pit;
      }

      double LIBERTY = this.LIBERTY_DESIRE * player_moves.size();
      double SEED_PTS = this.GREED * player_seeds;

      return LIBERTY_DESIRE + SEED_PTS;

    }

    private boolean pit_is_safe(int player, int pit, HusBoardState board){
      if (pit > board.BOARD_WIDTH / 2){
        return false;
      }
      else if (board.getPits()[this.player_id][board.BOARD_WIDTH - (pit + 1)] == 0){
        return true;
      }
      return false;
    }
}
