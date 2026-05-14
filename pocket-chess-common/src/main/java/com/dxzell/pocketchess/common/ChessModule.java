package com.dxzell.pocketchess.common;

import com.dxzell.pocketchess.api.game.ChessGameService;
import com.dxzell.pocketchess.api.move.MoveCalculator;
import com.dxzell.pocketchess.common.game.ChessGameServiceImpl;
import com.dxzell.pocketchess.common.move.MoveCalculatorImpl;
import com.dxzell.pocketchess.common.move.calculator.*;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public final class ChessModule extends AbstractModule {

  @Override
  public void configure() {
    bind(ChessGameService.class).to(ChessGameServiceImpl.class).in(Singleton.class);
    bind(MoveCalculator.class).to(MoveCalculatorImpl.class).in(Singleton.class);

    bind(PawnMoveCalculator.class).in(Singleton.class);
    bind(KnightMoveCalculator.class).in(Singleton.class);
    bind(BishopMoveCalculator.class).in(Singleton.class);
    bind(KingMoveCalculator.class).in(Singleton.class);
    bind(QueenMoveCalculator.class).in(Singleton.class);
    bind(RookMoveCalculator.class).in(Singleton.class);
  }
}
