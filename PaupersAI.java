import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;
import java.util.Random;
import java.util.Vector;

public class PaupersAI extends CKPlayer {

	public Point adj[];
	private int currdepth;
	public PaupersAI (byte player, BoardModel state) {
		super(player, state);
		teamName = "Paupers";
		this.currdepth = 0;
	}

	@Override
	public Point getMove(BoardModel state) {
//		for(int i=0; i<state.getWidth(); ++i)
//			for(int j=0; j<state.getHeight(); ++j)
//				if(state.getSpace(i, j) == 0)
//					return new Point(i,j);
//		return null;
		Random random = new Random();
		int i,j;
		while(true){
			i = random.nextInt(state.getWidth());
			j = random.nextInt(state.getHeight());
			if(state.getSpace(i, j) == 0)
				return new Point(i,j);
		}
	}
	
	public Point minimax(BoardModel state,byte player, int dlimit){
		//have to change player for minValue b/c opponent moves during minValue
		Point best = new Point();
		int v = Integer.MIN_VALUE;
		int prev = v;
		for (int row = 0; row < state.getWidth(); row++){
			for (int col = 0; col < state.getHeight(); col++){
				state = apply(new Point(row, col), player);
				this.currdepth++;
				v = Integer.max(v, minValue(state, player, dlimit));
				if(prev != v){
					best.setLocation(row, col); 
					prev = v;
				}
			}
		}
		return best;
	}
	
	private int maxValue(BoardModel state, byte player, int dlimit){
		if (currdepth == dlimit){
			this.currdepth--;
			return heuristic(state, player);
		}
		int v = Integer.MIN_VALUE;
		for (int row = 0; row < state.getWidth(); row++){
			for (int col = 0; col < state.getHeight(); col++){
				state = apply(new Point(row, col), player);
				this.currdepth++;
				v = Integer.max(v, minValue(state, player, dlimit));
			}
		}
		this.currdepth--;
		return v;
	}
	
	private int minValue(BoardModel state, byte player, int dlimit){
		if (currdepth == dlimit){
			this.currdepth--;
			return heuristic(state, player);
		}
		int v = Integer.MAX_VALUE;
		for (int row = 0; row < state.getWidth(); row++){
			for (int col = 0; col < state.getHeight(); col++){
				state = apply(new Point(row, col), player);
				this.currdepth++;
				v = Integer.min(v, maxValue(state, player, dlimit));
			}
		}
		this.currdepth--;
		return v;
	}
	
	private BoardModel apply(Point position,byte player){
		BoardModel copy = startState;
		return copy.placePiece(position, player);
	}
	private int heuristic(BoardModel state,byte player){
		int win = 0;
		Point[] adj = new Point[]{new Point(-1,-1),new Point(-1,0),new Point(-1,1),new Point(1,1),
									new Point(1,0),new Point(1,-1),new Point(0,1),new Point(0,-1)};
		for(int i=0; i<state.getWidth(); ++i){
			for(int j=0; j<state.getHeight(); ++j){
				if(state.getSpace(i, j) == player
						|| state.getSpace(i,j) == 0){
					for(Point p: adj){
						int in_a_row = 0;
						while(state.getSpace(i+p.x, j+p.y) == player || 
								state.getSpace(i+p.x, j+p.y) == 0){
							++in_a_row;
							if(in_a_row == state.kLength){
								++win;
								break;
							}
						}
					}
				}
			}
		}

		return win;
	}

	@Override
	public Point getMove(BoardModel state, int deadline) {
		return getMove(state);
	}
}
