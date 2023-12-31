package gui.view;

import java.util.EventObject;


/**
 * <p>Titre : Torcs Tune</p>
 * <p>Description : Torcs tuning</p>
 * <p>Copyright : Copyright (c) 2002 Patrice Espie</p>
 * <p>Societe : </p>
 * @author Patrice Espie
 * @version 0.1a
 */

public class CircuitViewSelectionEvent extends EventObject
{
    public CircuitView          circuitView;

    public CircuitViewSelectionEvent( CircuitView source )
    {
        super( source );

        circuitView = source;
    }
}
