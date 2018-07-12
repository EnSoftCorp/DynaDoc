package com.kcsl.dynadoc.html;

import com.hp.gagawa.java.FertileNode;

import com.hp.gagawa.java.Node;
import com.hp.gagawa.java.elements.Text;
import java.util.List;

public class Nav extends FertileNode {

	public Nav(){
		super("nav");
	}


	/**
	 * Appends a child node to the end of this element's DOM tree
	 * @param child node to be appended
	 * @return the node
	 */
	public Nav appendChild(Node child){
		if(this == child){
			throw new Error("Cannot append a node to itself.");
		}
		child.setParent(this);
		children.add(child);
		return this;
	}
	/**
	 * Appends a child node at the given index
	 * @param index insert point
	 * @param child node to be appended
	 * @return the node
	 */
	public Nav appendChild(int index, Node child){
		if(this == child){
			throw new Error("Cannot append a node to itself.");
		}
		child.setParent(this);
		children.add(index, child);
		return this;
	}
	/**
	 * Appends a list of children in the order given in the list
	 * @param children nodes to be appended
	 * @return the node
	 */
	public Nav appendChild(List<Node> children){
		if(children != null){;
			for(Node child: children){
				appendChild(child);
			}
		}
		return this;
	}
	/**
	 * Appends the given children in the order given
	 * @param children nodes to be appended
	 * @return the node
	 */
	public Nav appendChild(Node... children){
		for(int i = 0; i < children.length; i++){
			appendChild(children[i]);
		}
		return this;
	}
	/**
	 * Convenience method which appends a text node to this element
	 * @param text the text to be appended
	 * @return the node
	 */
	public Nav appendText(String text){
		return appendChild(new Text(text));
	}
	/**
	 * Removes the child node
	 * @param child node to be removed
	 * @return the node
	 */
	public Nav removeChild(Node child){
		children.remove(child);
		return this;
	}
	/**
	 * Removes all child nodes
	 * @return the node
	 */
	public Nav removeChildren(){
		children.clear();
		return this;
	}

	public Nav setType(String value){setAttribute("type", value); return this;}
	public String getType(){return getAttribute("type");}
	public boolean removeType(){return removeAttribute("type");}

	public Nav setId(String value){setAttribute("id", value); return this;}
	public String getId(){return getAttribute("id");}
	public boolean removeId(){return removeAttribute("id");}

	public Nav setCSSClass(String value){setAttribute("class", value); return this;}
	public String getCSSClass(){return getAttribute("class");}
	public boolean removeCSSClass(){return removeAttribute("class");}

	public Nav setTitle(String value){setAttribute("title", value); return this;}
	public String getTitle(){return getAttribute("title");}
	public boolean removeTitle(){return removeAttribute("title");}

	public Nav setStyle(String value){setAttribute("style", value); return this;}
	public String getStyle(){return getAttribute("style");}
	public boolean removeStyle(){return removeAttribute("style");}

	public Nav setDir(String value){setAttribute("dir", value); return this;}
	public String getDir(){return getAttribute("dir");}
	public boolean removeDir(){return removeAttribute("dir");}

	public Nav setLang(String value){setAttribute("lang", value); return this;}
	public String getLang(){return getAttribute("lang");}
	public boolean removeLang(){return removeAttribute("lang");}

	public Nav setXMLLang(String value){setAttribute("xml:lang", value); return this;}
	public String getXMLLang(){return getAttribute("xml:lang");}
	public boolean removeXMLLang(){return removeAttribute("xml:lang");}

}
