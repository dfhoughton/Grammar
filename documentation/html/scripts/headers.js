/**
 * Generates headers at top of page.
 */

function headers(self, base) {
	document.title = base + " :: " + self;
	var div = document.createElement("div");
	div.setAttribute("style", "margin: 1.5em auto; text-align: center");
	var nonInitial = false;
	for ( var key in hdrs) {
		if (key == self)
			continue;
		if (nonInitial) {
			var span = document.createElement("span");
			span.setAttribute('style', "margin: 0 1em");
			span.appendChild(document.createTextNode("|"));
			div.appendChild(span);
		} else
			nonInitial = true;
		var a = document.createElement("a");
		a.setAttribute("href", hdrs[key]);
		a.appendChild(document.createTextNode(key));
		div.appendChild(a);
	}
	document.body.appendChild(div);
}