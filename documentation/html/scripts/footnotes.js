/**
 * A little script to facilitate the use of footnotes on a page. Procedure:
 * 
 * 1) You create a style sheet with 3 styles
 * 
 * a) a.fn { display:none; ... } for the footnotes you write in-line
 * 
 * b) a.footnote { font-size: small; ... } for the footnotes automatically
 * created by this script
 * 
 * c) div#footnotes { margin-top: 1em; padding-top: 1em; border-top: thin solid
 * black; ... } for the section where the footnote text will appear
 * 
 * 2) In the body of your document wherever you want a footnote you enclose the
 * text like so: lorem ipsum<span class="fn">sic transit gloria mundi</span>
 * etc.
 * 
 * 3) You trigger footnotes() on page load like so: <body
 * onLoad="footnotes()">...
 * 
 * 4) If you want to control the placement of your footnotes, where you want to
 * footnotes to appear you include the following: <div id="footnotes"></div>.
 * Otherwise, the footnotes DIV will be appended to the list of child nodes of
 * the body element, so they will appear last.
 * 
 * Provided you have linked this script to the page it will modify the page
 * contents, replacing your invisible footnote anchor elements with links to
 * appropriate elements in the footnote section and vice versa. If it finds no
 * footnotes on the page, it will delete the footnotes section.
 * 
 * script © 2011, David F. Houghton
 * 
 * licensed under LGPL v3: http://www.gnu.org/copyleft/lesser.html
 */

function footnotes() {
	var fnSection = document.getElementById("footnotes");
	var fnsExists = fnSection && 1;
	if (!fnsExists) {
		fnSection = document.createElement('div');
		fnSection.setAttribute('id', 'footnotes');
	}
	var fns = document.getElementsByClassName("fn");
	// copy items to new array that won't be dynamically altered
	var ar = new Array();
	var len = fns.length;
	for ( var i = 0; i < len; i++) {
		var span = fns[i];
		ar[i] = span;
	}
	var found = false;
	var index = 1;
	for ( var i = 0; i < len; i++) {
		var span = ar[i];
		if (span.nodeName == 'SPAN') {
			found = true;
			span.removeAttribute('class');
			var bname = 'fn' + index + '_bottom';
			var tname = 'fn' + index + '_top';
			var topA = document.createElement("a");
			topA.setAttribute('href', '#' + bname);
			topA.setAttribute('name', tname);
			topA.setAttribute('class', 'footnote');
			topA.innerHTML = index;
			span.parentNode.replaceChild(topA, span);
			var textDiv = document.createElement("div");
			var bottomA = document.createElement("a");
			bottomA.setAttribute('href', '#' + tname);
			bottomA.setAttribute('name', bname);
			bottomA.setAttribute('class', 'footnote');
			bottomA.innerHTML = index;
			textDiv.appendChild(bottomA);
			textDiv.appendChild(document.createTextNode(' '));
			textDiv.appendChild(span);
			fnSection.appendChild(textDiv);
			index++;
		}
		if (found && !fnsExists)
			document.body.appendChild(fnSection);
	}
}