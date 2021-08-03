package dev.anarchy.ace;

public enum CodeSyntax {
	JAVA("ace/mode/java"),
	VELOCITY("ace/mode/velocity"),
	JSON("ace/mode/json"),
	;
	
	private String type;
	
	private CodeSyntax(String type) {
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}
}
