package student_player.mytools;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import java.util.ArrayList;
import java.lang.reflect.Constructor;
import java.util.Random;

public class MyTools {

  public static HusBoardState get_random_board(){

    HusBoardState shuffled_board = new HusBoardState();
    ArrayList<HusMove> legal_moves = shuffled_board.getLegalMoves();
    HusMove random_move;
    int legal_moves_size = legal_moves.size();

    for (int i = 0; i < 6; i++){
      if (legal_moves_size == 0){
        break;
      }
      else {
        random_move = legal_moves.get((new Random()).nextInt(legal_moves.size()));
        shuffled_board.move(random_move);
        legal_moves = shuffled_board.getLegalMoves();
        legal_moves_size = legal_moves.size();
      }
    }
    if (legal_moves_size < 8){
      shuffled_board = get_random_board();
    }
    return shuffled_board;
  }
}
