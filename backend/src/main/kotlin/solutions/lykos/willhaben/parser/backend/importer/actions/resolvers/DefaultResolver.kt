package solutions.lykos.willhaben.parser.backend.importer.actions.resolvers

import solutions.lykos.willhaben.parser.backend.importer.DefaultMapping
import solutions.lykos.willhaben.parser.backend.importer.basedata.Node

abstract class DefaultResolver<T : Node> : Resolver<T, DefaultMapping>()
