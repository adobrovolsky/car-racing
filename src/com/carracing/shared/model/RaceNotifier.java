package com.carracing.shared.model;

import com.carracing.shared.Command;
import com.carracing.shared.Command.Action;

public interface RaceNotifier {
	void onChange(final Action action, final Command command);
}
