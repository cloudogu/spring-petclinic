package org.springframework.samples.petclinic.system;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;

public abstract class BaseController {

	@Value("${STAGE}")
	private String stage;

	protected void addCommonAttributes(Model model) {
		model.addAttribute("stage", stage);
	}
}
