// Copyright (c) 2024 FRC 6328
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by an MIT-style
// license that can be found in the LICENSE file at
// the root directory of this project.

package frc.lib.dashboard;

import edu.wpi.first.networktables.*;
import java.util.Arrays;
import java.util.Objects;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.LoggedNetworkInput;

/** A string chooser for the dashboard where the options can be changed on-the-fly. */
public class SwitchableChooser extends LoggedNetworkInput {
  private static final String placeholder = "<NA>";

  private String[] options = new String[] {placeholder};
  private String active = placeholder;

  private final StringPublisher namePublisher;
  private final StringPublisher typePublisher;
  private final StringArrayPublisher optionsPublisher;
  private final StringPublisher defaultPublisher;
  private final StringPublisher activePublisher;
  private final StringPublisher selectedPublisher;
  private final StringSubscriber selectedInput;

  public SwitchableChooser(String name) {
    var table = NetworkTableInstance.getDefault().getTable("SmartDashboard").getSubTable(name);
    namePublisher = table.getStringTopic(".name").publish();
    typePublisher = table.getStringTopic(".type").publish();
    optionsPublisher = table.getStringArrayTopic("options").publish();
    defaultPublisher = table.getStringTopic("default").publish();
    activePublisher = table.getStringTopic("active").publish();
    selectedPublisher = table.getStringTopic("selected").publish();
    selectedInput = table.getStringTopic("selected").subscribe("<NA>", PubSubOption.periodic(0.1));
    Logger.registerDashboardInput(this);

    namePublisher.set(name);
    typePublisher.set("String Chooser");
    optionsPublisher.set(this.options);
    defaultPublisher.set(this.options[0]);
    activePublisher.set(this.options[0]);
    selectedPublisher.set(this.options[0]);
  }

  /** Updates the set of available options. */
  public void setOptions(String[] options) {
    if (Arrays.equals(options, this.options)) {
      return;
    }
    this.options = options.length == 0 ? new String[] {placeholder} : options;
    optionsPublisher.set(this.options);
    periodic();
  }

  /** Returns the selected option. */
  public String get() {
    return Objects.equals(active, placeholder) ? null : active;
  }

  @Override
  public void periodic() {
    String selected = selectedInput.get();
    active = null;
    for (String option : options) {
      if (!option.equals(placeholder) && option.equals(selected)) {
        active = option;
      }
    }
    if (active == null) {
      active = options[0];
      selectedPublisher.set(active);
    }
    defaultPublisher.set(active);
    activePublisher.set(active);
  }
}
